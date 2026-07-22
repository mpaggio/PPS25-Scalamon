package scalamon.logics.state

import scalamon.database.MoveDatabase.*
import scalamon.database.MyPokedex
import scalamon.domain.types.Type
import scalamon.domain.types.Type.*
import scalamon.domain.moves.MoveDSL.{move, *}
import scalamon.domain.moves.DamagingMove
import scalamon.domain.moves.DamageMoveCategory.*
import scalamon.domain.alteredStatus.AlteredStatus
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.weather.Weather
import scalamon.logics.state.MoveStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.BattleStateModuleImpl.*

/**
 * Layered fixtures: Each test class imports only the level it needs.
 * All cross-module imports are contained in this file, so individual tests
 * import only the module they are testing.
 */

trait MoveFixtures:
  val swift: DamagingMove =
    move named "Swift" withPower 60 withPP 32 withAccuracy 100 withType Normal as Special
  val swiftState: MoveState = moveInitialState(swift)

trait PokemonFixtures extends MoveFixtures:
  val charmanderSpecies: Pokemon = MyPokedex.allPokemons.find(_.name == "Charmander").get
  val bulbasaurSpecies: Pokemon = MyPokedex.allPokemons.find(_.name == "Bulbasaur").get
  val squirtleSpecies: Pokemon = MyPokedex.allPokemons.find(_.name == "Squirtle").get

  private def selectMovesFor(t: Type): Map[String, MoveState] =
    allMoves.ofType(t).take(4).map(m => m.name -> moveInitialState(m)).toMap

  val myPokemonMoves: Map[String, Ms] = selectMovesFor(charmanderSpecies.pokemonType)
  val myPokemon: Pks = pokemonInitialState(charmanderSpecies, myPokemonMoves) // HP 39, Atk 52, Def 43, Spe 65
  val mySecondPokemonMoves: Map[String, Ms] = selectMovesFor(bulbasaurSpecies.pokemonType)
  val mySecondPokemon: Pks = pokemonInitialState(bulbasaurSpecies, mySecondPokemonMoves) // HP 45, Atk 49, Def 49
  val enemyPokemonMoves: Map[String, Ms] = selectMovesFor(squirtleSpecies.pokemonType)
  val enemyPokemon: Pks = pokemonInitialState(squirtleSpecies, enemyPokemonMoves) // HP 44, Atk 48, Def 65

  val statusA: AlteredStatus = AlteredStatus.fromOrdinal(0)
  val statusB: AlteredStatus = AlteredStatus.fromOrdinal(1)

trait PlayerFixtures extends PokemonFixtures:
  val player1: Ps = playerInitialState(
    "Player1",
    Map("Charmander" -> myPokemon, "Bulbasaur" -> mySecondPokemon),
    "Charmander"
  )
  val player2: Ps = playerInitialState("Player2", Map("Squirtle" -> enemyPokemon), "Squirtle")

trait BattleFixtures extends PlayerFixtures:
  val battle: Bs = battleState(player1, player2)
  val alternativeWeather: Weather = Weather.Rain

/**
 * This trait is intended to be mixed into test classes that require access to the battle state fixtures.
 * It provides a convenient way to set up the necessary state for testing battle-related logic.
 */
trait StateFixtures extends BattleFixtures
