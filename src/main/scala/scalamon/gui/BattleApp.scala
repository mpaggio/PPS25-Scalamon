package scalamon.gui

import scalamon.logics.state.BattleStateImpl.BattleState
import scalamon.logics.state.BattleStateImpl.switchSelfOpponent
import scalamon.logics.state.PlayerStateModuleImpl.PlayerState
import scalamon.logics.state.DamagePolicy
import scalamon.logics.turns.*
import scalamon.logics.turns.TurnResult.*
import BattleWindowStateImpl.*
import scalamon.gui.SetupGUI.{GameSetup, buildAutomaticPlayerBuilder, buildOpponentBuilder, chooseGameSetup, damagePolicyFromChoice}
import scalamon.gui.ManualTeamBuildingGUI.chooseManualBuilder

@main def runScalamonGUI(): Unit =

  val (windowAfterSetup, gameSetup) = chooseGameSetup(initialWindow)

  given DamagePolicy = damagePolicyFromChoice(gameSetup.selectedDifficulty)

  val orchestrator = BattleOrchestrator()

  def speedOf(ps: PlayerState): Speed =
    Speed(ps.getActive.modifiedStats.speed)

  def firstAvailableMove(ps: PlayerState): String =
    ps.getActive.moves.find((_, ms) => ms.currentPp > 0).map(_._1)
      .getOrElse(throw RuntimeException("No available move!"))

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
    s"--- INITIAL SETUP ---\n" +
    s"Selected mode: $mode\n\n" +
    teamToString("TEAM PLAYER 1", bs.self) + "\n\n" +
    teamToString("TEAM PLAYER 2", bs.opponent) + "\n\n" +
    s"Initial lead: ${bs.self.getActive.species.name} vs ${bs.opponent.getActive.species.name}\n" +
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

  def windowCreation(initialInfo: String): State[Window, Unit] = for
    _ <- clear()
    _ <- setSize(500, 600)
    _ <- addLabel(initialInfo, "BattleStatus")
    _ <- addTextArea("", "BattleLog")
    _ <- addButton("Attack 1", "Move1")
    _ <- addButton("Attack 2", "Move2")
    _ <- addButton("Attack 3", "Move3")
    _ <- addButton("Attack 4", "Move4")
    _ <- addButton("Switch Pokemon", "SwitchMenu")
    _ <- addButton("Use item", "ItemMenu")
    _ <- show()
  yield ()

  def transitionScreen(message: String): State[Window, Unit] = State:
    w => javax.swing.JOptionPane.showMessageDialog(
      null,
      message,
      "Switch player!",
      javax.swing.JOptionPane.INFORMATION_MESSAGE
    )
    (w, ())

  def showSwitchMenu: State[(BattleState, Window), BattleAction] = State:
    case (bs, w) =>
      val available = bs.self.team.filter((id, p) => id != bs.self.activeId && p.currentHp > 0).keys.toList

      if (available.isEmpty)
        javax.swing.JOptionPane.showMessageDialog(null, "No available Pokemon")
        ((bs, w), UseMove(MoveRef(firstAvailableMove(bs.self))))
      else
        val selection = javax.swing.JOptionPane.showInputDialog(
          null, "Select a Pokemon to switch to:", "Switch",
          javax.swing.JOptionPane.QUESTION_MESSAGE, null,
          available.toArray.asInstanceOf[Array[Object]], available.head
        )
        val action = if selection != null then
          SwitchPokemon(PokemonRef(selection.toString))
        else
          UseMove(MoveRef("Skip"))

        ((bs, w), action)

  def showForcedSwitchMenu(message: String, candidates: List[PokemonRef]): State[(BattleState, Window), PokemonRef] = State:
    case (bs, w) =>
      val selection = javax.swing.JOptionPane.showInputDialog(
        null, "Select a Pokemon to switch to [mandatory]:", "MandatorySwitch",
        javax.swing.JOptionPane.WARNING_MESSAGE, null,
        candidates.map(_.value).toArray.asInstanceOf[Array[Object]], candidates.head.value
      )
      val chosenRef = if (selection != null) PokemonRef(selection.toString) else candidates.head
      ((bs, w), chosenRef)

  def showItemMenu: State[(BattleState, Window), BattleAction] = State:
    case (bs, w) =>
      val available = bs.self.items.map(_.name).toList

      if (available.isEmpty)
        javax.swing.JOptionPane.showMessageDialog(null, "No available items")
        ((bs, w), UseMove(MoveRef(firstAvailableMove(bs.self))))
      else
        val selection = javax.swing.JOptionPane.showInputDialog(
          null, "Select an item to use:", "Items",
          javax.swing.JOptionPane.QUESTION_MESSAGE, null,
          available.toArray.asInstanceOf[Array[Object]], available.head
        )
        val action = if selection != null then
          UseItem(selection.toString)
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
      case "ItemMenu" => showItemMenu
      case m if m.startsWith("Move") =>
        State[(BattleState, Window), BattleAction]:
          case (bs, w) =>
            val moveName = moveNameFromButton(m, bs.self)
            ((bs, w), UseMove(MoveRef(moveName)))
      case _ => getPlayerAction
  yield action

  def resolveHotSeatTurn(a1: BattleAction, a2: BattleAction): State[(BattleState, Window), TurnResult] = State:
    case (bs, w) =>
      val (result, newState) = orchestrator.runTurn(bs, TurnChoices(a1, a2), speedOf)

      w.updateTextArea(newState.logs.getLog, "BattleLog")
      w.updateLabel(battleStatusString(newState), "BattleStatus")

      ((newState, w), result)

  def switchPlayerPerspective: State[(BattleState, Window), Unit] =
    for
      _ <- mv(
        BattleStateAdapter.fromOp(switchSelfOpponent),
        _ => State.unit[Window,Unit](())
      )
      _ <- refreshMoveButtons
    yield ()

  def gameLoop: State[(BattleState, Window), Unit] = for
    action1 <- getPlayerAction
    _ <- mv(nop, _ => transitionScreen("Player2's turn. Change the controller!"))
    action2 <- for
      _ <- switchPlayerPerspective
      a <- getPlayerAction
      _ <- switchPlayerPerspective
    yield a

    result <- resolveHotSeatTurn(action1, action2)
    _ <- refreshMoveButtons

    _ <- result match
      case ForcedSwitch(candidates) =>
        for
          newPokemon <- showForcedSwitchMenu("Player1: Pokemon KO, select a substitute [mandatory]:", candidates)
          _ <- mv(BattleStateAdapter.fromOp(bs => orchestrator.applyForcedSwitch(bs, newPokemon)), _ => State.unit[Window, Unit](()))
          _ <- gameLoop
        yield ()

      case OpponentForcedSwitch(candidates) =>
        for
          newPokemon <- showForcedSwitchMenu("Player2: Pokemon KO, select a substitute [mandatory]:", candidates)
          _ <- mv(BattleStateAdapter.fromOp(bs => orchestrator.applyOpponentForcedSwitch(bs, newPokemon)), _ => State.unit[Window, Unit](()))
          _ <- gameLoop
        yield ()

      case BothForcedSwitch(candidates, oppCandidates) =>
        for
          newPokemon <- showForcedSwitchMenu("Player1: Pokemon KO, select a substitute [mandatory]:", candidates)
          _ <- mv(BattleStateAdapter.fromOp(bs => orchestrator.applyForcedSwitch(bs, newPokemon)), _ => State.unit[Window, Unit](()))
          newOpponentPokemon <- showForcedSwitchMenu("Player2: Pokemon KO, select a substitute [mandatory]:", oppCandidates)
          _ <- mv(BattleStateAdapter.fromOp(bs => orchestrator.applyOpponentForcedSwitch(bs, newOpponentPokemon)), _ => State.unit[Window, Unit](()))
          _ <- gameLoop
        yield ()

      case SelfWins =>
        mv(nop, _ => updateLabel("PLAYER 1 WINS", "BattleStatus"))
      case SelfLoses =>
        mv(nop, _ => updateLabel("PLAYER 2 WINS", "BattleStatus"))
      case _ =>
        gameLoop
  yield ()

  def startFullGame(): Unit =
    val selectedMode = gameSetup.selectedMode

    val (windowAfterManualSelection, playerBuilder) =
      selectedMode match
        case "Manual" => chooseManualBuilder(windowAfterSetup)
        case _        => (windowAfterSetup, buildAutomaticPlayerBuilder(selectedMode))

    val opponentBuilder = buildOpponentBuilder()
    val initialBattleState = BattleSetup.setupBattle(playerBuilder, opponentBuilder)

    val fullProgram: State[(BattleState, Window), Unit] = for
      _ <- setupState(selectedMode)
      _ <- gameLoop
    yield ()

    fullProgram.run((initialBattleState, windowAfterManualSelection))

  startFullGame()