package scalamon.logics.state

import org.scalatest.funsuite.AnyFunSuite

class PokemonStateTest extends AnyFunSuite:
  test("test damage and heal transformers"):
    import scalamon.logics.state.PokemonStateModuleImpl.*

    val pokemon = pokemonState(10)
    assert(pokemon.hp == 10)

    val damagedPokemon = pokemon damage 4
    assert(damagedPokemon.hp == 6)

    val healedPokemon = damagedPokemon heal 2
    assert(healedPokemon.hp == 8)