package scalamon.logics.turns

import org.scalatest.funsuite.AnyFunSuite
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.pokemon.abilities.Ability.Blaze
import scalamon.domain.pokemon.abilities.AbilitySlot
import scalamon.domain.pokemon.pokedex.PokedexADT
import scalamon.domain.pokemon.statistics.StatADT.fromInt
import scalamon.domain.pokemon.statistics.Stats
import scalamon.domain.types.Type.Grass
import scalamon.logics.state.BattleStateModuleImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*

class TurnFlowTest extends AnyFunSuite:

 private def makeStats: Stats =
   Stats(
     hp = fromInt(100),
     attack = fromInt(50),
     defense = fromInt(50),
     specialAttack = fromInt(50),
     specialDefense = fromInt(50),
     speed = fromInt(50)
   )

 private def makePokemon(name: String): Pokemon =
   Pokemon(
     pokedexId = PokedexADT.fromInt(1),
     name = name,
     pokemonType = Grass,
     baseStats = makeStats,
     abilitySlot = AbilitySlot(primary = Blaze)
   )

 private def makeBattleState: BattleState =
   val selfPokemon = pokemonInitialState(makePokemon("bulbasaur"), Map.empty)
   val opponentPokemon = pokemonInitialState(makePokemon("pikachu"), Map.empty)
   val selfPlayer = playerInitialState("Player1", Map("bulbasaur" -> selfPokemon), "bulbasaur")
   val opponentPlayer = playerInitialState("Player2", Map("pikachu" -> opponentPokemon), "pikachu")
   battleState(selfPlayer, opponentPlayer)

 test("TurnFlow orders player1 first when player1 has higher priority"):
   val state = makeBattleState
   val choices = TurnChoices(
     player1Action = UseMove(MoveRef("quick-attack"), priority = 1),
     player2Action = UseMove(MoveRef("tackle"))
   )
   val speedOf: PlayerState => Speed = _ => Speed(50)
   val result = TurnFlow.actionOrdering(state, choices, speedOf)
   assert(result == ActionOrder.Player1First)

 test("TurnFlow orders player2 first when player2 has higher priority"):
   val state = makeBattleState
   val choices = TurnChoices(
     player1Action = UseMove(MoveRef("tackle")),
     player2Action = UseMove(MoveRef("quick-attack"), priority = 1)
   )
   val speedOf: PlayerState => Speed = _ => Speed(50)
   val result = TurnFlow.actionOrdering(state, choices, speedOf)
   assert(result == ActionOrder.Player2First)

 test("TurnFlow orders by speed when priorities are equal"):
   val state = makeBattleState
   val choices = TurnChoices(
     player1Action = UseMove(MoveRef("tackle")),
     player2Action = UseMove(MoveRef("thunderbolt"))
   )
   val speedOf: PlayerState => Speed = _ => Speed(60)
   val result = TurnFlow.actionOrdering(state, choices, speedOf)
   assert(result == ActionOrder.Player1First)

 test("UseMove exposes its priority") {
   val action = UseMove(MoveRef("quick-attack"), priority = 1)
   assert(action.priority == 1)
 }

 test("SwitchPokemon has default priority 0") {
   val action = SwitchPokemon(PokemonRef("bulbasaur"))
   assert(action.priority == 0)
 }
