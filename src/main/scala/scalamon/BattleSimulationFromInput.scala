package scalamon

import scala.io.StdIn.readLine
import scalamon.domain.moves.Move
import scalamon.domain.moves.MoveDatabase.allMoves
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.pokemon.pokedex.MyPokedex.allPokemons
import scalamon.logics.state.BattleStateImpl.BattleState
import scalamon.logics.state.DamagePolicy
import scalamon.logics.state.PlayerStateModuleImpl.PlayerState
import scalamon.logics.turns.*
import scalamon.logics.turns.TurnResult.*
import scalamon.logics.teambuilder.ManualTeamBuilder.ManualTeamBuilder

import scala.annotation.tailrec

object BattleSimulationFromInput extends App:

  given DamagePolicy = DamagePolicy.Medium.given_DamagePolicy

  private val orchestrator = BattleOrchestrator()

  private def askInt(prompt: String, min: Int, max: Int): Int =
    var valid = false
    var result = min
    while !valid do
      print(prompt)
      readLine().trim.toIntOption match
        case Some(n) if n >= min && n <= max =>
          result = n
          valid = true
        case _ =>
          println(s"Inserisci un numero tra $min e $max.")
    result

  private def printIndexedList[A](values: List[A], label: A => String): Unit =
    values.zipWithIndex.foreach { case (value, i) =>
      println(s"${i + 1}. ${label(value)}")
    }

  private def chooseUniqueItems[A](
    howMany: Int,
    available: List[A],
    label: A => String,
    prompt: String
  ): List[A] =
    @tailrec
    def loop(chosen: List[A]): List[A] =
      if chosen.size == howMany then chosen
      else
        printIndexedList(available, label)
        val index = askInt(prompt, 1, available.size) - 1
        val picked = available(index)
        if chosen.exists(label(_) == label(picked)) then
          println(s"${label(picked)} e' gia' stato scelto, riprova.")
          loop(chosen)
        else
          loop(chosen :+ picked)
    loop(Nil)

  private def choosePokemons(playerName: String): List[Pokemon] =
    println(s"\n=== $playerName: scegli 6 Pokemon ===")
    chooseUniqueItems(
      howMany = 6,
      available = allPokemons,
      label = _.name,
      prompt = "Seleziona il numero del Pokemon: "
    )

  private def chooseMoves(playerName: String, pokemon: Pokemon): List[Move] =
    println(s"\n$playerName - scegli 4 mosse per ${pokemon.name}")
    chooseUniqueItems(
      howMany = 4,
      available = allMoves.toList,
      label = _.name,
      prompt = s"Seleziona una mossa per ${pokemon.name}: "
    )

  private def buildManualBuilder(playerName: String): ManualTeamBuilder =
    val chosenTeam: List[Pokemon] = choosePokemons(playerName)
    val selectedMoves: Map[String, List[Move]] =
      chosenTeam.map(p => p.name -> chooseMoves(playerName, p)).toMap

    ManualTeamBuilder(
      pokemonSelector = _ => chosenTeam,
      moveSelector = (pokemon, _) => selectedMoves(pokemon.name)
    )

  private def speedOf(playerState: PlayerState): Speed =
    Speed(playerState.getActive.modifiedStats.speed)

  private def printState(bs: BattleState, turn: Int): Unit =
    val selfActive = bs.self.getActive
    val opponentActive = bs.opponent.getActive
    println(
      s"[Turno $turn] " +
        s"Weather: ${bs.weather} | " +
        s"Player1(${bs.self.activeId}) HP: ${selfActive.currentHp} | " +
        s"Player2(${bs.opponent.activeId}) HP: ${opponentActive.currentHp}"
    )

  private def printTeams(state: BattleState): Unit =
    println("\nTEAM PLAYER1:")
    state.self.team.foreach { case (name, pokemonState) =>
      println(s"- $name -> ${pokemonState.moves.keys.mkString(", ")}")
    }
    println("\nTEAM PLAYER2:")
    state.opponent.team.foreach { case (name, pokemonState) =>
      println(s"- $name -> ${pokemonState.moves.keys.mkString(", ")}")
    }
    println()

  private def chooseSwitch(playerName: String, ps: PlayerState): PokemonRef =
    val availableBench = ps.team.keys.filterNot(_ == ps.activeId).toList
    println(s"\n$playerName - scegli il Pokemon da mandare in campo:")
    availableBench.zipWithIndex.foreach { case (name, i) =>
      println(s"${i + 1}. $name")
    }
    val choice = askInt("Scelta: ", 1, availableBench.size)
    PokemonRef(availableBench(choice - 1))

  private def chooseAction(playerName: String, ps: PlayerState): BattleAction =
    val active = ps.getActive
    val moves = active.moves.toList

    println(s"\n$playerName - Pokemon attivo: ${active.species.name}")
    println("Scegli un'azione:")
    moves.zipWithIndex.foreach { case ((moveName, moveState), i) =>
      println(s"${i + 1}. Usa $moveName (PP: ${moveState.currentPp})")
    }
    println(s"${moves.size + 1}. Cambia Pokemon")

    val choice = askInt("Scelta: ", 1, moves.size + 1)

    if choice <= moves.size then
      UseMove(MoveRef(moves(choice - 1)._1))
    else
      SwitchPokemon(chooseSwitch(playerName, ps))

  println("=== COSTRUZIONE SQUADRE ===")
  val builder1 = buildManualBuilder("Player1")
  val builder2 = buildManualBuilder("Player2")

  var state = BattleSetup.setupBattle(builder1, builder2)
  printTeams(state)

  println("INIZIO DELLA BATTAGLIA: PLAYER1 VS PLAYER2")

  private var turn = 1
  private var running = true

  while running do
    println(s"\n===== TURNO $turn =====")
    printState(state, turn)

    val player1Action = chooseAction("Player1", state.self)
    val player2Action = chooseAction("Player2", state.opponent)

    val (newState, result) = orchestrator.runTurn(state, TurnChoices(player1Action, player2Action), speedOf)
    state = newState

    println(newState.logs.getLog)

    result match
      case Ongoing(_) => ()

      case ForcedSwitch(_, candidates) =>
        val koName = state.self.getActive.species.name
        println(s"$koName e' andato KO!")
        candidates.headOption match
          case Some(_) =>
            val chosen = chooseSwitch("Player1", state.self)
            println(s"Player1 manda in campo ${chosen.value}")
            state = orchestrator.applyForcedSwitch(state, chosen)
          case None =>
            running = false

      case OpponentForcedSwitch(_, candidates) =>
        val koName = state.opponent.getActive.species.name
        println(s"$koName e' andato KO!")
        candidates.headOption match
          case Some(_) =>
            val chosen = chooseSwitch("Player2", state.opponent)
            println(s"Player2 manda in campo ${chosen.value}")
            state = orchestrator.applyOpponentForcedSwitch(state, chosen)
          case None =>
            running = false

      case BothForcedSwitch(_, selfCandidates, opponentCandidates) =>
        println(s"${state.self.getActive.species.name} e' andato KO!")
        println(s"${state.opponent.getActive.species.name} e' andato KO!")

        selfCandidates.headOption match
          case Some(_) =>
            val chosen = chooseSwitch("Player1", state.self)
            println(s"Player1 manda in campo ${chosen.value}!")
            state = orchestrator.applyForcedSwitch(state, chosen)
          case None =>
            running = false

        opponentCandidates.headOption match
          case Some(_) =>
            val chosen = chooseSwitch("Player2", state.opponent)
            println(s"Player2 manda in campo ${chosen.value}!")
            state = orchestrator.applyOpponentForcedSwitch(state, chosen)
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