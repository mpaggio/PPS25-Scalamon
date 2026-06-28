package scalamon

import scalamon.domain.moves.MoveDatabase
import scalamon.domain.moves.MoveDatabase.findByName
import scalamon.domain.pokemon.pokedex.MyPokedex
import scalamon.logics.state.BattleStateImpl.{BattleState, battleState}
import scalamon.logics.state.{DamagePolicy, PlayerStateModuleImpl}
import scalamon.logics.state.MoveStateModuleImpl.moveInitialState
import scalamon.logics.state.PlayerStateModuleImpl.playerState
import scalamon.logics.state.PokemonStateModuleImpl.pokemonInitialState
import scalamon.logics.turns.BattleAction.UseMove
import scalamon.logics.turns.{ActionOrderResolver, BattleOrchestrator, MoveRef, PokemonRef, Speed, TrainerId, TurnChoices, TurnFlow, TurnResult}
import scalamon.logics.turns.TurnResult.*

object BattleSimulation extends App:

  // SETUP INIZIALE

  given DamagePolicy = DamagePolicy.Medium.given_DamagePolicy

  val pokedex = MyPokedex.allPokemons
  private def findPokemon(name: String) = pokedex.find(_.name == name).get

  private val ember = MoveDatabase.allMoves.findByName("Ember").get
  private val flamethrower = MoveDatabase.allMoves.findByName("Flamethrower").get
  private val surf = MoveDatabase.allMoves.findByName("Surf").get
  private val bubbleBeam = MoveDatabase.allMoves.findByName("Bubble beam").get

  private val charmander = pokemonInitialState(findPokemon("Charmander"), Map("Ember" -> moveInitialState(ember), "Flamethrower" -> moveInitialState(flamethrower)))
  private val charizard = pokemonInitialState(findPokemon("Charizard"), Map("Flamethrower" -> moveInitialState(flamethrower)))
  private val squirtle = pokemonInitialState(findPokemon("Squirtle"), Map("Surf" -> moveInitialState(surf), "Bubble beam" -> moveInitialState(bubbleBeam)))
  private val blastoise = pokemonInitialState(findPokemon("Blastoise"), Map("Surf" -> moveInitialState(surf)))

  val player1 = playerState(Map("Charmander" -> charmander, "Charizard" -> charizard), "Charmander")
  val player2 = playerState(Map("Squirtle" -> squirtle, "Blastoise" -> blastoise), "Squirtle")

  var state = battleState(player1, player2)

  // ORCHESTRATOR

  private val orchestrator = BattleOrchestrator(TurnFlow(ActionOrderResolver.default))

  // HELPER UTILI

  private def firstAvailableMove(ps: PlayerStateModuleImpl.PlayerState): String =
    ps.getActive.moves
      .find((_, ms) => ms.currentPp > 0)
      .map(_._1)
      .getOrElse(throw RuntimeException(s"${ps.getActive.species.name} HA ESAURITO TUTTI I PP!"))

  private def speedOf(ref: PokemonRef): Speed =
    val inSelf = state.self.team.get(ref.value)
    val inOpponent = state.opponent.team.get(ref.value)
    val pokemon = (inSelf orElse inOpponent).getOrElse(
      throw RuntimeException(s"Pokemon ${ref.value} non trovato in nessuna squadra!")
    )
    val baseSpeed = pokemon.modifiedStats.speed
    Speed(baseSpeed)

  private def printState(bs: BattleState, turn: Int): Unit =
    val s = bs.self.getActive
    val o = bs.opponent.getActive
    println(s"[Turno $turn] Player1(${bs.self.activeId}) HP: ${s.currentHp} | Player2(${bs.opponent.activeId}) HP: ${o.currentHp}")

  // LOOP DI GIOCO

  println("INIZIO DELLA BATTAGLIA: PLAYER1 VS PLAYER2")

  private var turn = 1
  private var running = true

  while running do
    println(s"TURNO $turn")

    val move1Name = firstAvailableMove(state.self)
    val move2Name = firstAvailableMove(state.opponent)

    val player1Action = UseMove(TrainerId("Player1"), PokemonRef(state.self.activeId), PokemonRef(state.opponent.activeId),
                        MoveRef(move1Name), priority = 0)
    val player2Action = UseMove(TrainerId("Player2"), PokemonRef(state.opponent.activeId), PokemonRef(state.self.activeId),
                        MoveRef(move2Name), priority = 0)

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
            println(s" Player2 manda in campo ${newActive.value}")
            state = orchestrator.applyOpponentForcedSwitch(state, newActive)
          case None =>
            running = false

      case BothForcedSwitch(_, selfCandidates, opponentCandidates) =>
        println(s"\n${state.self.getActive.species.name} e' andato KO!")
        println(s"\n${state.opponent.getActive.species.name} e' andato KO!")
        selfCandidates.headOption match
          case Some(newActive) =>
            println(s"Player1 manda in campo ${newActive.value}!")
            state = orchestrator.applyForcedSwitch(state, newActive)
          case None => running = false
        opponentCandidates.headOption match
          case Some(newActive) =>
            print(s"Player2 manda in campo ${newActive.value}!")
            state = orchestrator.applyOpponentForcedSwitch(state, newActive)
          case None => running = false

      case SelfWins(finalState) =>
        if finalState.opponent.getActive.currentHp <= 0 then
          println(s"${finalState.opponent.getActive.species.name} e' andato KO!")
        println(s"Player2 non ha piu' Pokemon a disposizione!")
        println("PLAYER1 VINCE!")
        running = false
      case SelfLoses(finalState) =>
        if finalState.self.getActive.currentHp <= 0 then
          println(s"${finalState.self.getActive.species.name} e' andato KO!")
        println(s"Player1 non ha piu' Pokemon a disposizione!")
        println("PLAYER2 VINCE!")
        running = false

    turn += 1