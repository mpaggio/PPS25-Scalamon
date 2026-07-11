package scalamon.gui

import scalamon.logics.state.BattleStateImpl.BattleState
import scalamon.logics.state.BattleStateImpl.switchSelfOpponent
import scalamon.logics.state.PlayerStateModuleImpl.PlayerState
import scalamon.logics.state.DamagePolicy
import scalamon.logics.turns.*
import scalamon.logics.turns.TurnResult.*
import BattleWindowStateImpl.*
import scalamon.gui.SetupGUI.{GameSetup, buildAutomaticPlayerBuilder, chooseGameSetup, damagePolicyFromChoice}
import scalamon.gui.ManualTeamBuildingGUI.chooseManualBuilder

@main def runScalamonGUI(): Unit =

  val (windowAfterSetup, gameSetup) = chooseGameSetup(initialWindow)

  given DamagePolicy = damagePolicyFromChoice(gameSetup.selectedDifficulty)

  val orchestrator = BattleOrchestrator()

  def speedOf(ps: PlayerState): Speed =
    Speed(ps.getActive.modifiedStats.speed)

  def activeMoveNames(ps: PlayerState): List[String] =
    ps.getActive.moves.toList.map(_._1).take(4)

  def activeMoveInfo(ps: PlayerState): List[String] =
    ps.getActive.moves.take(4).map((name, move) => s"<html>$name<br>(PP = ${move.currentPp}/${move.move.pp})</html>").toList

  def moveNameFromButton(buttonName: String, ps: PlayerState): String =
    val moves = activeMoveNames(ps)
    val idx = buttonName.stripPrefix("Move").toInt - 1
    moves(idx)

  def refreshMoveButtons: State[(BattleState, Window), Unit] =
    State { case (bs, w) =>
      val moves = activeMoveInfo(bs.self)
      w.updateButtonText(moves.headOption.getOrElse("-"), "Move1")
      w.updateButtonText(moves.lift(1).getOrElse("-"), "Move2")
      w.updateButtonText(moves.lift(2).getOrElse("-"), "Move3")
      w.updateButtonText(moves.lift(3).getOrElse("-"), "Move4")
      ((bs, w), ())
    }

  def battleStatusString(bs: BattleState): String =
    s"${bs.self.getActive.species.name}" +
    s" HP:${bs.self.getActive.currentHp}/${bs.self.getActive.species.baseStats.hp}" +
    s" vs ${bs.opponent.getActive.species.name}" +
    s" HP:${bs.opponent.getActive.currentHp}/${bs.opponent.getActive.species.baseStats.hp}"

  def teamToString(title: String, ps: PlayerState): String =
    val teamDetails = ps.team.map{
      case (name, pokemonState) => s"- $name -> ${pokemonState.moves.keys.mkString(", ")}"
    }.mkString("\n")
    s"$title:\n$teamDetails"

  def weatherString(bs: BattleState): String =
    s"Current Weather: ${bs.weather}"

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
    _ <- addLabel("Weather: ClearSky", "WeatherStatus")
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

  def showSwitchMenu: State[(BattleState, Window), Option[BattleAction]] = State:
    case (bs, w) =>
      val available = bs.self.team.filter((id, p) => id != bs.self.activeId && p.currentHp > 0).keys.toList

      if (available.isEmpty)
        javax.swing.JOptionPane.showMessageDialog(null, "No available Pokemon")
        ((bs, w), None)
      else
        val selection = javax.swing.JOptionPane.showInputDialog(
          null, "Select a Pokemon to switch to:", "Switch",
          javax.swing.JOptionPane.QUESTION_MESSAGE, null,
          available.toArray.asInstanceOf[Array[Object]], available.head
        )
        if selection != null then
          ((bs, w), Some(SwitchPokemon(PokemonRef(selection.toString))))
        else
          ((bs, w), None)

  def showForcedSwitchMenu(message: String, candidates: List[PokemonRef]): State[(BattleState, Window), PokemonRef] = State:
    case (bs, w) =>
      val selection = javax.swing.JOptionPane.showInputDialog(
        null, "Select a Pokemon to switch to [mandatory]:", "MandatorySwitch",
        javax.swing.JOptionPane.WARNING_MESSAGE, null,
        candidates.map(_.value).toArray.asInstanceOf[Array[Object]], candidates.head.value
      )
      val chosenRef = if (selection != null) PokemonRef(selection.toString) else candidates.head
      ((bs, w), chosenRef)

  def showItemMenu: State[(BattleState, Window), Option[BattleAction]] = State:
    case (bs, w) =>
      val available = bs.self.items.map(_.name).toList

      if (available.isEmpty)
        javax.swing.JOptionPane.showMessageDialog(null, "No available items")
        ((bs, w), None)
      else
        val selection = javax.swing.JOptionPane.showInputDialog(
          null, "Select an item to use:", "Items",
          javax.swing.JOptionPane.QUESTION_MESSAGE, null,
          available.toArray.asInstanceOf[Array[Object]], available.head
        )
        if selection != null then
          ((bs, w), Some(UseItem(selection.toString)))
        else
          ((bs, w), None)

  def setupState(selectedMode: String): State[(BattleState, Window), Unit] = for
    _ <- mv(getStatus, windowCreation)
    _ <- State[(BattleState, Window), Unit] :
      case (bs, w) =>
        w.updateTextArea(getInitialSetupLog(selectedMode, bs), "BattleLog")
        w.updateLabel(weatherString(bs), "WeatherStatus")
        ((bs, w), ())
    _ <- refreshMoveButtons
  yield ()

  def getPlayerAction: State[(BattleState, Window), BattleAction] = for
    event <- mv(nop, _ => nextEvent())
    action <- event match
      case "SwitchMenu" =>
        for
          result <- showSwitchMenu
          action <- result match
            case Some(a) => State.unit[(BattleState, Window), BattleAction](a)
            case None => getPlayerAction
        yield action

      case "ItemMenu" =>
        for
          result <- showItemMenu
          action <- result match
            case Some(a) => State.unit[(BattleState, Window), BattleAction](a)
            case None => getPlayerAction
        yield action

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
      w.updateLabel(weatherString(newState), "WeatherStatus")

      ((newState, w), result)

  def switchPlayerPerspective: State[(BattleState, Window), Unit] =
    for
      _ <- mv(
        BattleStateAdapter.fromOp(switchSelfOpponent),
        _ => State.unit[Window,Unit](())
      )
      _ <- refreshMoveButtons
    yield ()

  def handleTurnResult(result: TurnResult): State[(BattleState, Window), Unit] = State:
    case (bs, w) => result match
        case ForcedSwitch(candidates) =>
          val ((nextBs, nextW), choice) =
            showForcedSwitchMenu("Player1: Pokemon KO, select a substitute [mandatory]:", candidates).run((bs, w))
          val stateAfterSwitch = orchestrator.applyForcedSwitch(nextBs, choice)
          w.updateTextArea(stateAfterSwitch.logs.getLog, "BattleLog")
          w.updateLabel(battleStatusString(stateAfterSwitch), "BattleStatus")
          w.updateLabel(weatherString(stateAfterSwitch), "WeatherStatus")
          ((stateAfterSwitch, nextW), ())

        case OpponentForcedSwitch(candidates) =>
          val ((nextBs, nextW), choice) =
            showForcedSwitchMenu("Player2: Pokemon KO, select a substitute [mandatory]:", candidates).run((bs, w))
          val stateAfterSwitch = orchestrator.applyOpponentForcedSwitch(nextBs, choice)
          w.updateTextArea(stateAfterSwitch.logs.getLog, "BattleLog")
          w.updateLabel(battleStatusString(stateAfterSwitch), "BattleStatus")
          w.updateLabel(weatherString(stateAfterSwitch), "WeatherStatus")
          ((stateAfterSwitch, nextW), ())

        case BothForcedSwitch(candidates, oppCandidates) =>
          val ((s1, w1), c1) = showForcedSwitchMenu("Player 1, choose replacement:", candidates).run((bs, w))
          val ((s2, w2), c2) = showForcedSwitchMenu("Player 2, choose replacement:", oppCandidates).run((s1, w1))
          val finalState = orchestrator.applyOpponentForcedSwitch(orchestrator.applyForcedSwitch(s2, c1), c2)
          w.updateTextArea(finalState.logs.getLog, "BattleLog")
          w.updateLabel(battleStatusString(finalState), "BattleStatus")
          w.updateLabel(weatherString(finalState), "WeatherStatus")
          ((finalState, w2), ())

        case _ => ((bs, w), ())

  def gameLoop: State[(BattleState, Window), Unit] = for
    action1 <- getPlayerAction
    _ <- mv(nop, _ => transitionScreen("Player2's turn. Change the controller!"))
    action2 <- for
      _ <- switchPlayerPerspective
      a <- getPlayerAction
      _ <- switchPlayerPerspective
    yield a

    result <- resolveHotSeatTurn(action1, action2)
    _ <- handleTurnResult(result)
    _ <- refreshMoveButtons
    _ <- result match
      case SelfWins =>
        mv(nop, _ => transitionScreen("PLAYER 1 WINS"))
      case SelfLoses =>
        mv(nop, _ => transitionScreen("PLAYER 2 WINS"))
      case _ =>
        gameLoop
  yield ()

  def startFullGame(): Unit =
    val selectedMode = gameSetup.selectedMode

    val (windowAfterBuilders, playerBuilder, opponentBuilder) =
      selectedMode match
        case "Manual" =>
          val (w1, player1Builder) = chooseManualBuilder(windowAfterSetup, "Player 1")
          val (w2, player2Builder) = chooseManualBuilder(w1, "Player 2")
          (w2, player1Builder, player2Builder)
        case _ =>
          val builder1 = buildAutomaticPlayerBuilder(selectedMode)
          val builder2 = buildAutomaticPlayerBuilder(selectedMode)
          (windowAfterSetup, builder1, builder2)

    val initialBattleState = BattleSetup.setupBattle(playerBuilder, opponentBuilder)

    val fullProgram: State[(BattleState, Window), Unit] = for
      _ <- setupState(selectedMode)
      _ <- gameLoop
    yield ()

    fullProgram.run((initialBattleState, windowAfterBuilders))

  startFullGame()