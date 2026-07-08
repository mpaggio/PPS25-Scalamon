package scalamon

import scalamon.domain.moves.MoveDatabase
import scalamon.domain.moves.MoveDatabase.{allMoves, findByName}
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.pokemon.pokedex.MyPokedex
import scalamon.domain.pokemon.pokedex.MyPokedex.allPokemons
import scalamon.logics.state.BattleStateImpl.BattleState
import scalamon.logics.state.{DamagePolicy, PlayerStateModuleImpl}
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.turns.UseMove
import scalamon.logics.turns.*
import scalamon.logics.turns.TurnResult.*
import scalamon.logics.teambuilder.ManualTeamBuilder.ManualTeamBuilder

object BattleSimulation extends App:
/*
  // SETUP INIZIALE

  given DamagePolicy = DamagePolicy.Medium.given_DamagePolicy

  private def pokemonNamed(name: String): Pokemon =
    allPokemons.find(_.name == name).getOrElse(
      throw new NoSuchElementException(s"Pokemon with name $name not found")
    )

  private def moveNamed(name: String): Move =
    allMoves.findByName(name).getOrElse(
      throw new NoSuchElementException(s"Move with name $name not found")
    )

  private val team1 = List(
    pokemonNamed("Bulbasaur"),
    pokemonNamed("Charmander"),
    pokemonNamed("Squirtle"),
    pokemonNamed("Venusaur"),
    pokemonNamed("Charizard"),
    pokemonNamed("Blastoise")
  )

  private val team2 = List(
    pokemonNamed("Pikachu"),
    pokemonNamed("Gengar"),
    pokemonNamed("Mewtwo"),
    pokemonNamed("Jolteon"),
    pokemonNamed("Hypno"),
    pokemonNamed("Arbok")
  )

  private val builder1 = ManualTeamBuilder(
    pokemonSelector = _ => team1,
    moveSelector = (_, _) => List(
      moveNamed("Body slam"),
      moveNamed("Hyper beam"),
      moveNamed("Double edge"),
      moveNamed("Slash")
    )
  )

  private val builder2 = ManualTeamBuilder(
    pokemonSelector = _ => team2,
    moveSelector = (_, _) => List(
      moveNamed("Swift"),
      moveNamed("Strength"),
      moveNamed("Recover"),
      moveNamed("Ember")
    )
  )

  // ORCHESTRATOR

  private val orchestrator = BattleOrchestrator()

  // HELPER UTILI

  private def firstAvailableMove(ps: PlayerState): String =
    ps.getActive.moves
      .find((_, ms) => ms.currentPp > 0)
      .map(_._1)
      .getOrElse(throw RuntimeException(s"${ps.getActive.species.name} HA ESAURITO TUTTI I PP!"))

  private def speedOf(playerState: PlayerState): Speed =
    val baseSpeed = playerState.getActive.modifiedStats.speed
    Speed(baseSpeed)

  private def printState(bs: BattleState, turn: Int): Unit =
    val s = bs.self.getActive
    val o = bs.opponent.getActive
    println(s"[Turno $turn] Player1(${bs.self.activeId}) HP: ${s.currentHp} | Player2(${bs.opponent.activeId}) HP: ${o.currentHp}")

  var state = BattleSetup.setupBattle(builder1, builder2)

  println("TEAM PLAYER1:")
  state.self.team.foreach { case (name, pokemonState) =>
    println(s"- $name -> ${pokemonState.moves.keys.mkString(", ")}")
  }

  println()
  println("TEAM PLAYER2:")
  state.opponent.team.foreach { case (name, pokemonState) =>
    println(s"- $name -> ${pokemonState.moves.keys.mkString(", ")}")
  }
  println()

  // LOOP DI GIOCO

  println("INIZIO DELLA BATTAGLIA: PLAYER1 VS PLAYER2")

  private var turn = 1
  private var running = true

  while running do
    println(s"TURNO $turn")

    val move1Name = firstAvailableMove(state.self)
    val move2Name = firstAvailableMove(state.opponent)

    val player1Action = UseMove(MoveRef(move1Name))
    val player2Action = UseMove(MoveRef(move2Name))

    val (newState, result) = orchestrator.runTurn(state, TurnChoices(player1Action, player2Action), speedOf)
    state = newState

    printState(state, turn)

    result match
      case Ongoing(_) => ()

      case ForcedSwitch(_, candidates) =>
        val koName = state.self.getActive.species.name
        println(s"$koName e' andato KO!")
        candidates.headOption match
          case Some(newActive) =>
            println(s"Player1 manda in campo ${newActive.value}")
            state = orchestrator.applyForcedSwitch(state, newActive)
          case None =>
            running = false

      case OpponentForcedSwitch(_, candidates) =>
        val koName = state.opponent.getActive.species.name
        println(s"$koName e' andato KO!")
        candidates.headOption match
          case Some(newActive) =>
            println(s"Player2 manda in campo ${newActive.value}")
            state = orchestrator.applyOpponentForcedSwitch(state, newActive)
          case None =>
            running = false

      case BothForcedSwitch(_, selfCandidates, opponentCandidates) =>
        println(s"${state.self.getActive.species.name} e' andato KO!")
        println(s"${state.opponent.getActive.species.name} e' andato KO!")

        selfCandidates.headOption match
          case Some(newActive) =>
            println(s"Player1 manda in campo ${newActive.value}!")
            state = orchestrator.applyForcedSwitch(state, newActive)
          case None =>
            running = false

        opponentCandidates.headOption match
          case Some(newActive) =>
            println(s"Player2 manda in campo ${newActive.value}!")
            state = orchestrator.applyOpponentForcedSwitch(state, newActive)
          case None =>
            running = false

      case SelfWins(finalState) =>
        if finalState.opponent.getActive.currentHp <= 0 then
          println(s"${finalState.opponent.getActive.species.name} e' andato KO!")
        println("Player2 non ha piu' Pokemon a disposizione!")
        println("PLAYER1 VINCE!")
        running = false

      case SelfLoses(finalState) =>
        if finalState.self.getActive.currentHp <= 0 then
          println(s"${finalState.self.getActive.species.name} e' andato KO!")
        println("Player1 non ha piu' Pokemon a disposizione!")
        println("PLAYER2 VINCE!")
        running = false

    turn += 1
 */