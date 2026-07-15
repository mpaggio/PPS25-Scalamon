package scalamon.app

import scalamon.logics.state.BattleStateImpl.{BattleState, switchSelfOpponent}
import scalamon.logics.state.DamagePolicy
import scalamon.logics.state.PlayerStateModuleImpl.PlayerState
import scalamon.logics.teambuilder.AffineTeamBuilder.AffineTeamBuilder
import scalamon.logics.teambuilder.ManualTeamBuilder.ManualTeamBuilder
import scalamon.logics.teambuilder.RandomTeamBuilder.RandomTeamBuilder
import scalamon.logics.teambuilder.TeamBuilder.TeamBuilder
import scalamon.logics.turns.*
import scalamon.logics.turns.TurnResult.*
import scalamon.util.StateMonad
import scalamon.util.StateMonad.*

/**
 * The application layer: it orchestrates setup, team building and the
 * hot-seat game loop. It only depends on the GameView port, so any
 * interface (Swing, terminal, ...) can be plugged in without touching
 * this class.
 *
 * The whole game is a computation over the pair (BattleState, view.V);
 * `battle` and `ui` lift computations on each half into the combined state.
 */
final class GameApp(val view: GameView):
  import GameConfig.*

  private type Game[A] = StateMonad[(BattleState, view.V), A]

  private def battle[A](m: StateMonad[BattleState, A]): Game[A] = m.onFirst[view.V]
  private def ui[A](m: StateMonad[view.V, A]): Game[A] = m.onSecond[BattleState]

  private val players: List[String] = List(Side.Self, Side.Opponent).map(Presenter.sideLabel)
  
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

  private def actionPrompt(bs: BattleState): ActionPrompt =
    ActionPrompt(
      moves = Presenter.moveSlots(bs.self.getActive),
      switchable = bs.self.team.filter((id, p) => id != bs.self.activeId && p.currentHp > 0).keys.toList,
      items = bs.self.items.toList.map(i => ItemSlot(i.name, i.description))
    )

  private def manualBuilder(player: String): StateMonad[view.V, TeamBuilder] = for
    choosePokemonTeam <- view.chooseTeam(player)
    chooseMoves <- view.chooseMoves(player)
    chooseItems <- view.chooseItems(player)

  yield
    ManualTeamBuilder(choosePokemonTeam, chooseMoves, chooseItems)

  private def automaticBuilder(mode: Mode): TeamBuilder = mode match
    case Mode.Affine => AffineTeamBuilder()
    case _           => RandomTeamBuilder()

  private def teamBuilders(mode: Mode): StateMonad[view.V, (TeamBuilder, TeamBuilder)] = mode match
    case Mode.Manual =>
      for
        first  <- manualBuilder(players.head)
        second <- manualBuilder(players.last)
      yield (first, second)
    case automatic => StateMonad.unit((automaticBuilder(automatic), automaticBuilder(automatic)))
  
  private def refreshView: Game[Unit] = for
    bs <- battle(StateMonad.get[BattleState])
    _  <- ui(view.renderBattle(viewModel(bs)))
  yield ()

  private def playerAction: Game[BattleAction] = for
    bs     <- battle(StateMonad.get[BattleState])
    intent <- ui(view.askAction(actionPrompt(bs)))
  yield toAction(intent)

  /** Runs a computation from the opponent's perspective, restoring it. */
  private def asOpponent[A](m: Game[A]): Game[A] = for
    _ <- battle(StateMonad.modify(switchSelfOpponent))
    a <- m
    _ <- battle(StateMonad.modify(switchSelfOpponent))
  yield a

  private def hotSeatChoices: Game[TurnChoices] = for
    first  <- playerAction
    _      <- ui(view.announce(s"${players.last}'s turn. Change the controller!"))
    second <- asOpponent(playerAction)
  yield TurnChoices(first, second)

  private def resolveTurn(orchestrator: BattleOrchestrator, choices: TurnChoices): Game[TurnResult] = for
    result <- battle(StateMonad[BattleState, TurnResult](bs =>
      val (result, next) = orchestrator.runTurn(bs, choices, speedOf)
      (next, result)
    ))
    _ <- refreshView
  yield result

  private def forcedSwitchChoice(request: SwitchRequest): Game[(Side, PokemonRef)] =
    ui(view.askForcedSwitch(Presenter.forcedSwitchMessage(request.side), request.candidates.map(_.value)))
      .map(chosen => request.side -> PokemonRef(chosen))

  private def handleForcedSwitch(orchestrator: BattleOrchestrator, result: TurnResult): Game[Unit] =
    result match
      case ForcedSwitch(requests) =>
        for
          choices <- StateMonad.traverse(requests)(forcedSwitchChoice)
          _       <- battle(StateMonad.modify(orchestrator.applyForcedSwitches(choices)))
          _       <- refreshView
        yield ()
      case _ => StateMonad.unit(())

  private def endGame(winner: Side): Game[Unit] = for
    _ <- ui(view.announce(s"${Presenter.sideLabel(winner).toUpperCase} WINS"))
    _ <- ui(view.close)
  yield ()

  private def gameLoop(orchestrator: BattleOrchestrator): Game[Unit] = for
    choices <- hotSeatChoices
    result  <- resolveTurn(orchestrator, choices)
    _       <- handleForcedSwitch(orchestrator, result)
    _ <- result match
      case Victory(winner) => endGame(winner)
      case _               => gameLoop(orchestrator)
  yield ()

  def run(): Unit =
    val setup = for
      difficulty <- view.chooseDifficulty
      mode       <- view.chooseMode
      builders   <- teamBuilders(mode)
    yield (difficulty, mode, builders)

    val (readyView, (difficulty, mode, (playerBuilder, opponentBuilder))) = setup.run(view.initial)

    given DamagePolicy = damagePolicyOf(difficulty)
    val orchestrator = BattleOrchestrator()
    val initialState = BattleSetup.setupBattle(playerBuilder, opponentBuilder)

    val program: Game[Unit] = for
      bs <- battle(StateMonad.get[BattleState])
      _  <- ui(view.showBattleScreen(viewModel(bs), Presenter.initialSetupLog(mode, bs)))
      _  <- gameLoop(orchestrator)
    yield ()

    program.run((initialState, readyView))
