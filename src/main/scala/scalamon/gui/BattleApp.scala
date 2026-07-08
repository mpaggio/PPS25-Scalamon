package scalamon.gui

import scalamon.domain.moves.MoveDatabase.{allMoves, findByName}
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.pokemon.pokedex.MyPokedex.allPokemons
import scalamon.logics.state.BattleStateImpl.BattleState
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
    println(s"Modalità selezionata: $mode")
    println()
    printTeam("TEAM PLAYER1", bs.self)
    println()
    printTeam("TEAM PLAYER2", bs.opponent)
    println()
    println(s"Lead iniziale: ${bs.self.getActive.species.name} vs ${bs.opponent.getActive.species.name}")
    println()

  def applyPlayerMove(state: BattleState, buttonName: String): (BattleState, String) =
    val move1Name = moveNameFromButton(buttonName, state.self)
    val move2Name = firstAvailableMove(state.opponent)
    val ((newState, logger), result) = orchestrator.runTurn(
      state,
      TurnChoices(UseMove(MoveRef(move1Name)), UseMove(MoveRef(move2Name))),
      speedOf
    )
    println(logger)
    val message = result match
      case Ongoing(_) => battleStatusString(newState)
      case SelfWins(_) => "PLAYER1 VINCE!"
      case SelfLoses(_) => "PLAYER2 VINCE!"
      case _ => battleStatusString(newState) + " | Cambio forzato!"
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
    _ <- addLabel(initialInfo, "BattleLog")
    _ <- addButton("Attacco 1", "Move1")
    _ <- addButton("Attacco 2", "Move2")
    _ <- addButton("Attacco 3", "Move3")
    _ <- addButton("Attacco 4", "Move4")
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

  val (windowAfterChoice, selectedMode) = chooseModeScreen.run(initialWindow)

  val playerBuilder = buildPlayerBuilder(selectedMode)
  val opponentBuilder = buildOpponentBuilder()
  val initialBattleState = BattleSetup.setupBattle(playerBuilder, opponentBuilder)

  printInitialSetupLog(selectedMode, initialBattleState)

  val fullProgram: State[(BattleState, Window), Unit] = for
    _ <- setupState
    _ <- gameLoop
  yield ()

  fullProgram.run((initialBattleState, windowAfterChoice))