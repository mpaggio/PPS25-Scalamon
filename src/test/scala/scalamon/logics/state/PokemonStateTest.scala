package scalamon.logics.state

import org.scalatest.funsuite.AnyFunSuite
import scalamon.domain.pokemon.pokedex.MyPokedex
import scalamon.domain.pokemon.statistics.StatADT.fromInt
import scalamon.domain.pokemon.statistics.Stats
import scalamon.logics.state.StatsStateModuleImpl.statsInitialState

class PokemonStateTest extends AnyFunSuite:
  test("test damage and heal transformers"):
    import scalamon.logics.state.PokemonStateModuleImpl.*



    val myPokemon = pokemonInitialState(MyPokedex.allPokemons.head)

    assert(myPokemon.currentHp == 10)

    val damagedPokemon = myPokemon damage 4
    assert(damagedPokemon.currentHp == 6)

    val healedPokemon = damagedPokemon heal 2
    assert(healedPokemon.currentHp == 8)