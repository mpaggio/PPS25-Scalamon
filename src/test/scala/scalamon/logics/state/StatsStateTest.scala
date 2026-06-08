package scalamon.logics.state

import org.scalatest.funsuite.AnyFunSuite
import scalamon.domain.pokemon.pokedex.MyPokedex
import scalamon.domain.pokemon.statistics.StatADT.fromInt
import scalamon.logics.state.PokemonStateModuleImpl.pokemonInitialState

class StatsStateTest extends AnyFunSuite:
  test("test stat modifiers"):
    import scalamon.logics.state.StatsStateModuleImpl.*
    import scalamon.domain.pokemon.statistics.Stats

    type StatModifier = Stats => Stats

    val stats = pokemonInitialState(MyPokedex.allPokemons.head).modifiedStats


    assert(stats.hp == fromInt(10))
    assert(stats.attack == fromInt(6))

    val weakModifier: StatModifier = _ attack (_ decrease 2)
    val armorModifier: StatModifier = _ defense (_ increase 2)
    val slowModifier: StatModifier = _ speed (_ multiply 0.5)

    val newStats = weakModifier andThen armorModifier andThen slowModifier apply stats

    assert(newStats.hp == fromInt(10))
    assert(newStats.attack == fromInt(4))
    assert(newStats.defense == fromInt(5))
    assert(newStats.speed == fromInt(3))