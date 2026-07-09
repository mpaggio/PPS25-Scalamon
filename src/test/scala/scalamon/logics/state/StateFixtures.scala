package scalamon.logics.state
import scalamon.domain.pokemon.pokedex.MyPokedex
import scalamon.domain.moves.MoveDatabase.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.MoveStateModuleImpl.*
import scalamon.domain.types.Type

trait StateFixtures:
  val charmanderSpecies = MyPokedex.allPokemons.find(_.name == "Charmander").get
  val bulbasaurSpecies = MyPokedex.allPokemons.find(_.name == "Bulbasaur").get
  val squirtleSpecies = MyPokedex.allPokemons.find(_.name == "Squirtle").get
  
  private def selectMovesFor(t: Type): Map[String, MoveState] =
    allMoves.ofType(t).take(4).map(m => m.name -> moveInitialState(m)).toMap
  
  val myPokemonMoves = selectMovesFor(charmanderSpecies.pokemonType)
  val myPokemon = pokemonInitialState(charmanderSpecies, myPokemonMoves) // HP 39, Atk 52, Def 43
  val mySecondPokemonMoves = selectMovesFor(bulbasaurSpecies.pokemonType)
  val mySecondPokemon = pokemonInitialState(bulbasaurSpecies, mySecondPokemonMoves) // HP 45, Atk 49, Def 49
  val enemyPokemonMoves = selectMovesFor(squirtleSpecies.pokemonType)
  val enemyPokemon = pokemonInitialState(squirtleSpecies, enemyPokemonMoves) // HP 44, Atk 48, Def 65
  val player1 = playerInitialState(Map("Charmander" -> myPokemon, "Bulbasaur" -> mySecondPokemon), "Charmander")
  val player2 = playerInitialState(Map("Squirtle" -> enemyPokemon), "Squirtle")
  val battle = battleState(player1, player2)

