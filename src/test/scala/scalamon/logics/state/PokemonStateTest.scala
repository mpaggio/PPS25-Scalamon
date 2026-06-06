package scalamon.logics.state

import org.scalatest.funsuite.AnyFunSuite
import scalamon.logics.state.StatsStateModuleImpl.statState
import scalamon.domain.pokemon.Stats

class PokemonStateTest extends AnyFunSuite:
  test("test damage and heal transformers"):
    import scalamon.logics.state.PokemonStateModuleImpl.*
    
    val stats = statState(Stats(hp = 10, attack = 6, defense = 3, specialAttack = 4, specialDefense = 2, speed = 6))

    val pokemon = pokemonState(10, stats)
    assert(pokemon.hp == 10)

    val damagedPokemon = pokemon damage 4
    assert(damagedPokemon.hp == 6)

    val healedPokemon = damagedPokemon heal 2
    assert(healedPokemon.hp == 8)