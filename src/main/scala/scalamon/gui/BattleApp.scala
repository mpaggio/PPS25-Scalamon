package scalamon.gui

import scalamon.logics.state.BattleStateImpl.BattleState
import scalamon.logics.state.PlayerStateModuleImpl.PlayerState
import scalamon.logics.state.DamagePolicy
import scalamon.logics.turns.*
import scalamon.logics.turns.TurnResult.*
import BattleWindowStateImpl.*
import scalamon.gui.SetupGUI.{chooseGameSetup, damagePolicyFromChoice, buildOpponentBuilder, buildAutomaticPlayerBuilder, GameSetup}
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

  def handleMoveClick(buttonName: String): State[(BattleState, Window), Unit] = for
    _ <- mv(applyMove(buttonName), msg => updateLabel(msg, "BattleLog"))
    _ <- refreshMoveButtons
  yield ()

  def battleStatusString(bs: BattleState): String =
    s"${bs.self.getActive.species.name} HP:${bs.self.getActive.currentHp} vs ${bs.opponent.getActive.species.name} HP:${bs.opponent.getActive.currentHp}"

  def printTeam(title: String, ps: PlayerState): Unit =
    println(title)
    ps.team.foreach { case (name, pokemonState) =>
      println(s"- $name -> ${pokemonState.moves.keys.mkString(", ")}")
    }

  def printInitialSetupLog(mode: String, bs: BattleState): Unit =
    println(s"Selected Mode: $mode")
    println()
    printTeam("TEAM PLAYER1", bs.self)
    println()
    printTeam("TEAM PLAYER2", bs.opponent)
    println()
    println(s"Lead Pokemon: ${bs.self.getActive.species.name} vs ${bs.opponent.getActive.species.name}")
    println()

  def applyPlayerMove(state: BattleState, buttonName: String): (BattleState, String) =
    val move1Name = moveNameFromButton(buttonName, state.self)
    val move2Name = firstAvailableMove(state.opponent)
    val (newState, result) = orchestrator.runTurn(
      state,
      TurnChoices(UseMove(MoveRef(move1Name)), UseMove(MoveRef(move2Name))),
      speedOf
    )
    println(newState.logs.getLog)
    val message = result match
      case Ongoing(_) => battleStatusString(newState)
      case SelfWins(_) => "PLAYER1 WINS!"
      case SelfLoses(_) => "PLAYER2 WINS!"
      case _ => battleStatusString(newState) + " | Needs Forced Switch!"
    (newState, message)

  def getStatus: State[BattleState, String] =
    State(bs => (bs, battleStatusString(bs)))

  def applyMove(buttonName: String): State[BattleState, String] =
    State(bs => applyPlayerMove(bs, buttonName))

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
    _ <- setSize(500, 400)
    _ <- addLabel(initialInfo, "BattleLog")
    _ <- addButton("Attack 1", "Move1")
    _ <- addButton("Attack 2", "Move2")
    _ <- addButton("Attack 3", "Move3")
    _ <- addButton("Attack 4", "Move4")
    _ <- show()
  yield ()

  def setupState: State[(BattleState, Window), Unit] = for
    _ <- mv(getStatus, windowCreation)
    _ <- refreshMoveButtons
  yield ()

  def gameLoop: State[(BattleState, Window), Unit] = for
    event <- mv(nop, _ => nextEvent())
    _ <- event match
      case "Move1" => handleMoveClick("Move1")
      case "Move2" => handleMoveClick("Move2")
      case "Move3" => handleMoveClick("Move3")
      case "Move4" => handleMoveClick("Move4")
      case _       => State.unit[(BattleState, Window), Unit](())
    _ <- gameLoop
  yield ()

  val selectedMode = gameSetup.selectedMode

  val (windowAfterManualSelection, playerBuilder) =
    selectedMode match
      case "Manual" => chooseManualBuilder(windowAfterSetup)
      case _        => (windowAfterSetup, buildAutomaticPlayerBuilder(selectedMode))


  val opponentBuilder = buildOpponentBuilder()
  val initialBattleState = BattleSetup.setupBattle(playerBuilder, opponentBuilder)

  printInitialSetupLog(selectedMode, initialBattleState)

  val fullProgram: State[(BattleState, Window), Unit] = for
    _ <- setupState
    _ <- gameLoop
  yield ()

  fullProgram.run((initialBattleState, windowAfterManualSelection))