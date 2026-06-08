package scalamon.logics.state
import scalamon.domain.pokemon.pokedex.MyPokedex
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.BattleStateImpl.*

trait StateFixtures:
  val charmanderSpecies = MyPokedex.allPokemons.find(_.name == "Charmander").get
  val bulbasaurSpecies = MyPokedex.allPokemons.find(_.name == "Bulbasaur").get
  val squirtleSpecies = MyPokedex.allPokemons.find(_.name == "Squirtle").get
  val myPokemon = pokemonInitialState(charmanderSpecies) // HP 39, Atk 52, Def 43
  val mySecondPokemon = pokemonInitialState(bulbasaurSpecies) // HP 45, Atk 49, Def 49
  val enemyPokemon = pokemonInitialState(squirtleSpecies) // HP 44, Atk 48, Def 65
  val player1 = playerState(Map("Pikachu" -> myPokemon, "Charmander" -> mySecondPokemon), "Pikachu")
  val player2 = playerState(Map("Bulbasaur" -> enemyPokemon), "Bulbasaur")
  val battle = battleState(player1, player2)

