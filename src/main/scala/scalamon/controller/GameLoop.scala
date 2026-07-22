package scalamon.controller

import scalamon.logics.damage.DamagePolicy
import scalamon.logics.state.BattleStateModuleImpl.BattleState
import scalamon.logics.state.PlayerStateModuleImpl.PlayerState
import scalamon.logics.teambuilder.*
import scalamon.logics.teambuilder.TeamBuilder.TeamBuilder
import scalamon.logics.turns.*
import scalamon.logics.turns.TurnResult.*
import scalamon.util.StateMonad
import scalamon.util.StateMonad.*

/**
 * The application layer: it orchestrates setup, team building and the
 * hot-seat game loop.
 *
 * The whole game is a computation over the pair (BattleState, view.V);
 * `battle` and `ui` lift computations on each half into the combined state.
 */
final class GameLoop(val view: ViewModel):
  import Presenter.playerNames

  private type Game[A] = StateMonad[(BattleState, view.V), A]

  private def battle[A](m: StateMonad[BattleState, A]): Game[A] = m.onFirst[view.V]
  private def ui[A](m: StateMonad[view.V, A]): Game[A] = m.onSecond[BattleState]
  
  private def speedOf(ps: PlayerState): Speed = Speed(ps.getActive.modifiedStats.speed)
  
  private def damagePolicyOf(difficulty: Difficulty): DamagePolicy = difficulty match
    case Difficulty.Easy   => DamagePolicy.Easy.given_DamagePolicy
    case Difficulty.Medium => DamagePolicy.Medium.given_DamagePolicy
    case Difficulty.Hard   => DamagePolicy.Hard.given_DamagePolicy
  
  private def toAction(intent: PlayerIntent): BattleAction = intent match
    case PlayerIntent.Attack(move)    => UseMove(MoveRef(move))
    case PlayerIntent.Switch(pokemon) => SwitchPokemon(PokemonRef(pokemon))
    case PlayerIntent.Item(item)      => UseItem(item)

  private def viewModel(bs: BattleState): BattleViewModel =
    BattleViewModel(
      status = Presenter.status(bs),
      weather = Presenter.weather(bs),
      log = bs.logs.getLog,
      moves = Presenter.moveSlots(bs.self.getActive)
    )

  private def manualBuilder(name: String): StateMonad[view.V, TeamBuilder] = for
    choosePokemonTeam <- view.chooseTeam(name)
    chooseMoves <- view.chooseMoves(name)
    chooseItems <- view.chooseItems(name)
  yield ManualTeamBuilder(choosePokemonTeam, chooseMoves, chooseItems)

  private def builderFor(mode: Mode, name: String): StateMonad[view.V, (TeamBuilder, String)] =
    val builder = mode match
      case Mode.Manual => manualBuilder(name)
      case Mode.Random => StateMonad.unit(RandomTeamBuilder)
      case Mode.Affine => StateMonad.unit(AffineTeamBuilder)
    builder.map((_, name))

  private def playerSetups(mode: Mode): StateMonad[view.V, List[(TeamBuilder, String)]] =
    StateMonad.traverse(playerNames.toList)(builderFor(mode, _))

  private def refreshView: Game[Unit] = for
    bs <- battle(get)
    _  <- ui(view.renderBattle(viewModel(bs)))
  yield ()

  private def playerAction(playerState: PlayerState): Game[BattleAction] =
    val actionPrompt = ActionPrompt(
      moves = Presenter.moveSlots(playerState.getActive),
      switchable = Utilities.aliveBench(playerState).map(_.value),
      items = playerState.items.toList.map(i => ItemSlot(i.name, i.description))
    )
    ui(view.askAction(actionPrompt)).map(toAction)

  private def hotSeatChoices: Game[TurnChoices] = for
    bs     <- battle(get)
    first  <- playerAction(bs.self)
    _      <- ui(view.announce(s"${playerNames._2}'s turn. Change the controller!"))
    second <- playerAction(bs.opponent)
  yield TurnChoices(first, second)

  private def resolveTurn(orchestrator: BattleOrchestrator, choices: TurnChoices): Game[TurnResult] = for
    result <- StateMonad(orchestrator.runTurn(_, choices, speedOf)).onFirst
    _ <- refreshView
  yield result

  private def forcedSwitchChoice(request: SwitchRequest): Game[(Side, PokemonRef)] =
    ui(view.askForcedSwitch(Presenter.forcedSwitchMessage(request.side), request.candidates.map(_.value)))
      .map(request.side -> PokemonRef(_))

  private def handleForcedSwitch(orchestrator: BattleOrchestrator, result: TurnResult): Game[Unit] =
    result match
      case ForcedSwitch(requests) =>
        for
          choices <- StateMonad.traverse(requests)(forcedSwitchChoice)
          _ <- battle(StateMonad.modify(orchestrator.applyForcedSwitches(choices)))
          _ <- refreshView
        yield ()
      case _ => StateMonad.unit(())

  private def endGame(winnerName: String): Game[Unit] = for
    _ <- ui(view.announce(s"${winnerName.toUpperCase} WINS"))
    _ <- ui(view.close)
  yield ()

  private def gameLoop(orchestrator: BattleOrchestrator): Game[Unit] = for
    choices <- hotSeatChoices
    result  <- resolveTurn(orchestrator, choices)
    _ <- handleForcedSwitch(orchestrator, result)
    _ <- result match
      case Victory(winnerName) => endGame(winnerName)
      case _ => gameLoop(orchestrator)
  yield ()

  def run(): Unit =
    val setup = for
      difficulty <- view.chooseDifficulty
      mode <- view.chooseMode
      builders <- playerSetups(mode)
    yield (difficulty, mode, builders)

    val (readyView, (difficulty, mode, builders)) = setup.run(view.initial)

    given DamagePolicy = damagePolicyOf(difficulty)
    val orchestrator = BattleOrchestrator()
    val initialState = BattleSetup.setupBattle(builders.head, builders.last)

    val program: Game[Unit] = for
      bs <- battle(get)
      _  <- ui(view.showBattleScreen(viewModel(bs), Presenter.initialSetupLog(mode, bs)))
      _  <- gameLoop(orchestrator)
    yield ()

    program.run((initialState, readyView))
