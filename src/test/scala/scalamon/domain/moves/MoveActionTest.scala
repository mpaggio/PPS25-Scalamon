package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import DamageMoveCategory.*
import MoveDSL.{move, *}
import scalamon.domain.types.Type.*
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.domain.pokemon.pokedex.MyPokedex.*
import MoveActionModuleImpl.*

class MoveActionTest extends org.scalatest.funsuite.AnyFunSuite:

  val pokemon: PokemonSpecies = allPokemons.last
  val pokemonStartingState: PokemonState = pokemonInitialState(pokemon)
  val swift: DamagingMove = move named "Swift" withPower 60 withPP 32 withAccuracy 100 withType Normal as Special
  val battleStartingState: BattleState = battleState(
    userPokemon = playerState(Map.from(List(pokemon.name -> pokemonStartingState)), pokemon.name),
    enemyPokemon = playerState(Map.from(List(pokemon.name -> pokemonStartingState)), pokemon.name)
  )

  test("Move action with 100% accuracy should always decrease enemy HP"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    val action: MoveAction = MoveAction(swift)
    val battleStateTransformations: Action = action(battleStartingState)
    val finalState = battleStateTransformations.foldLeft(battleStartingState)((state, transformer) => transformer(state))
    finalState.opponent.team(pokemon.name).currentHp.asInstanceOf[Int] < pokemon.baseStats.hp.toInt