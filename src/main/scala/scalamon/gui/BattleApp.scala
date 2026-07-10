package scalamon.gui

import scalamon.domain.moves.MoveDatabase.{allMoves, findByName}
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.pokemon.pokedex.MyPokedex.allPokemons
import scalamon.logics.state.BattleStateImpl.{BattleState, switchSelfOpponent}
import scalamon.logics.state.PlayerStateModuleImpl.PlayerState
import scalamon.logics.state.DamagePolicy
import scalamon.logics.turns.{BattleOrchestrator, BattleSetup, Speed, *}
import scalamon.logics.turns.TurnResult.*
import scalamon.logics.teambuilder.TeamBuilder.TeamBuilder
import scalamon.logics.teambuilder.ManualTeamBuilder.ManualTeamBuilder
import scalamon.logics.teambuilder.RandomTeamBuilder.RandomTeamBuilder
import scalamon.logics.teambuilder.AffineTeamBuilder.AffineTeamBuilder
import BattleWindowStateImpl.*

@main def runScalamonGUI(): Unit =

  given DamagePolicy = DamagePolicy.Medium.given_DamagePolicy

  val orchestrator = BattleOrchestrator()

  def pokemonNamed(name: String): Pokemon =
    allPokemons.find(_.name == name).getOrElse(throw new NoSuchElementException(name))

  def moveNamed(name: String) =
    allMoves.findByName(name).getOrElse(throw new NoSuchElementException(name))

  val fixedTeam = List("Bulbasaur", "Charmander", "Squirtle", "Venusaur", "Charizard", "Blastoise").map(pokemonNamed)

  val fallbackManualBuilder = ManualTeamBuilder(
    pokemonSelector = _ => fixedTeam,
    moveSelector = (_, _) => List("Body slam", "Hyper beam", "Double edge", "Slash").map(moveNamed)
  )

  def buildPlayerBuilder(mode: String): TeamBuilder = mode match
    case "Random" => RandomTeamBuilder()
    case "Affine" => AffineTeamBuilder()
    case "Manual" => fallbackManualBuilder
    case _        => RandomTeamBuilder()

  def buildOpponentBuilder(): TeamBuilder =
    RandomTeamBuilder()

  def speedOf(ps: PlayerState): Speed =
    Speed(ps.getActive.modifiedStats.speed)

  def firstAvailableMove(ps: PlayerState): String =
    ps.getActive.moves.find((_, ms) => ms.currentPp > 0).map(_._1)
      .getOrElse(throw RuntimeException("Nessuna mossa disponibile"))

  def activeMoveNames(ps: PlayerState): List[String] =
    ps.getActive.moves.toList.map(_._1).take(4)

  def moveNameFromButton(buttonName: String, ps: PlayerState): String =
    val moves = activeMoveNames(ps)
    val idx = buttonName.stripPrefix("Move").toInt - 1
    moves(idx)

  def refreshMoveButtons: State[(BattleState, Window), Unit] =
    State { case (bs, w) =>
      val moves = activeMoveNames(bs.self)
      w.updateButtonText(moves.headOption.getOrElse("-"), "Move1")
      w.updateButtonText(moves.lift(1).getOrElse("-"), "Move2")
      w.updateButtonText(moves.lift(2).getOrElse("-"), "Move3")
      w.updateButtonText(moves.lift(3).getOrElse("-"), "Move4")
      ((bs, w), ())
    }

  def battleStatusString(bs: BattleState): String =
    s"${bs.self.getActive.species.name} HP:${bs.self.getActive.currentHp} vs ${bs.opponent.getActive.species.name} HP:${bs.opponent.getActive.currentHp}"

  def teamToString(title: String, ps: PlayerState): String =
    val teamDetails = ps.team.map{
      case (name, pokemonState) => s"- $name -> ${pokemonState.moves.keys.mkString(", ")}"
    }.mkString("\n")
    s"$title:\n$teamDetails"

  def getInitialSetupLog(mode: String, bs: BattleState): String =
    s"--- SETUP INIZIALE ---\n" +
    s"Modalità selezionata: $mode\n\n" +
    teamToString("TEAM PLAYER 1", bs.self) + "\n\n" +
    teamToString("TEAM PLAYER 2", bs.opponent) + "\n\n" +
    s"Lead iniziale: ${bs.self.getActive.species.name} vs ${bs.opponent.getActive.species.name}\n" +
    s"-----------------------\n\n"

  def getStatus: State[BattleState, String] =
    State(bs => (bs, battleStatusString(bs)))

  def nop: State[BattleState, Unit] =
    State(bs => (bs, ()))

  def mv[SM, SV, AM, AV](m1: State[SM, AM], f: AM => State[SV, AV]): State[(SM, SV), AV] =
    State { case (sm, sv) =>
      val (sm2, am) = m1.run(sm)
      val (sv2, av) = f(am).run(sv)
      ((sm2, sv2), av)
    }

  def setupScreen: State[Window, Unit] = for
    _ <- clear()
    _ <- setSize(420, 260)
    _ <- addLabel("Scegli team building", "SetupTitle")
    _ <- addButton("Manual", "Manual")
    _ <- addButton("Random", "Random")
    _ <- addButton("Affine", "Affine")
    _ <- show()
  yield ()

  def chooseModeScreen: State[Window, String] = for
    _     <- setupScreen
    event <- nextEvent()
  yield event

  def windowCreation(initialInfo: String): State[Window, Unit] = for
    _ <- clear()
    _ <- setSize(500, 400)
    _ <- addLabel(initialInfo, "BattleStatus")
    _ <- addTextArea("", "BattleLog")
    _ <- addButton("Attacco 1", "Move1")
    _ <- addButton("Attacco 2", "Move2")
    _ <- addButton("Attacco 3", "Move3")
    _ <- addButton("Attacco 4", "Move4")
    _ <- addButton("Scambio Pokemon", "SwitchMenu")
    _ <- show()
  yield ()

  def transitionScreen(message: String): State[Window, Unit] = State:
    w => javax.swing.JOptionPane.showMessageDialog(
      null,
      message,
      "Cambio giocatore",
      javax.swing.JOptionPane.INFORMATION_MESSAGE
    )
    (w, ())

  def showSwitchMenu: State[(BattleState, Window), BattleAction] = State:
    case (bs, w) =>
      val available = bs.self.team.filter((id, p) => id != bs.self.activeId && p.currentHp > 0).keys.toList

      if (available.isEmpty)
        javax.swing.JOptionPane.showMessageDialog(null, "Non hai altri Pokemon disponibili")
        ((bs, w), UseMove(MoveRef(firstAvailableMove(bs.self))))
      else
        val selection = javax.swing.JOptionPane.showInputDialog(
          null, "Scegli Pokémon da mandare in campo:", "Cambio",
          javax.swing.JOptionPane.QUESTION_MESSAGE, null,
          available.toArray.asInstanceOf[Array[Object]], available.head
        )
        val action = if selection != null then
          SwitchPokemon(PokemonRef(selection.toString))
        else
          UseMove(MoveRef("Skip"))

        ((bs, w), action)

  def setupState(selectedMode: String): State[(BattleState, Window), Unit] = for
    _ <- mv(getStatus, windowCreation)
    _ <- State[(BattleState, Window), Unit] :
      case (bs, w) =>
        w.updateTextArea(getInitialSetupLog(selectedMode, bs), "BattleLog")
        ((bs, w), ())
    _ <- refreshMoveButtons
  yield ()

  def getPlayerAction: State[(BattleState, Window), BattleAction] = for
    event <- mv(nop, _ => nextEvent())
    action <- event match
      case "SwitchMenu" => showSwitchMenu
      case m if m.startsWith("Move") =>
        State[(BattleState, Window), BattleAction]:
          case (bs, w) =>
            val moveName = moveNameFromButton(m, bs.self)
            ((bs, w), UseMove(MoveRef(moveName)))
      case _ => getPlayerAction
  yield action

  def resolveHotSeatTurn(a1: BattleAction, a2: BattleAction): State[(BattleState, Window), TurnResult] = State:
    case (bs, w) =>
      val ((newState, logger), result) = orchestrator.runTurn(bs, TurnChoices(a1, a2), speedOf)

      w.updateTextArea(logger.getLog, "BattleLog")
      w.updateLabel(battleStatusString(newState), "BattleStatus")

      ((newState, w), result)

  def switchPlayerPerspective: State[(BattleState, Window), Unit] =
    for
      _ <- mv(
        BattleStateAdapter.fromOp(switchSelfOpponent),
        _ => State.unit(())
      )
      _ <- refreshMoveButtons
    yield ()

  def gameLoop: State[(BattleState, Window), Unit] = for
    action1 <- getPlayerAction
    _ <- mv(nop, _ => transitionScreen("Tocca al Player 2. Passa il dispositivo!"))
    action2 <- for
      _ <- switchPlayerPerspective
      a <- getPlayerAction
      _ <- switchPlayerPerspective
    yield a
    result <- resolveHotSeatTurn(action1, action2)
    _ <- refreshMoveButtons

    _ <- result match
      case SelfWins(_) =>
        mv(nop, _ => updateLabel("VITTORIA PER IL PLAYER 1", "BattleStatus"))
      case SelfLoses(_) =>
        mv(nop, _ => updateLabel("VITTORIA PER IL PLAYER 2", "BattleStatus"))
      case _ =>
        gameLoop
  yield ()

  def startFullGame(): Unit =
    val (windowAfterChoice, selectedMode) = chooseModeScreen.run(initialWindow)

    val playerBuilder = buildPlayerBuilder(selectedMode)
    val opponentBuilder = buildOpponentBuilder()
    val initialBattleState = BattleSetup.setupBattle(playerBuilder, opponentBuilder)

    val fullProgram: State[(BattleState, Window), Unit] = for
      _ <- setupState(selectedMode)
      _ <- gameLoop
    yield ()

    fullProgram.run((initialBattleState, windowAfterChoice))

  startFullGame()