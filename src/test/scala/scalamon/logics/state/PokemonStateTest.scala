package scalamon.logics.state

import org.scalatest.funsuite.AnyFunSuite
import scalamon.domain.pokemon.statistics.StatADT.fromInt
import scalamon.domain.pokemon.statistics.Stats
import scalamon.logics.state.StatsStateModuleImpl.statState

class PokemonStateTest extends AnyFunSuite:
  test("test damage and heal transformers"):
    import scalamon.logics.state.PokemonStateModuleImpl.*
    
    val stats = statState(Stats(
    hp = fromInt(10),
    attack = fromInt(6),
    defense = fromInt(3),
    specialAttack = fromInt(4),
    specialDefense = fromInt(2),
    speed = fromInt(6)
  ))

    val pokemon = pokemonState(10, stats)
    assert(pokemon.hp == 10)

    val damagedPokemon = pokemon damage 4
    assert(damagedPokemon.hp == 6)

    val healedPokemon = damagedPokemon heal 2
    assert(healedPokemon.hp == 8)