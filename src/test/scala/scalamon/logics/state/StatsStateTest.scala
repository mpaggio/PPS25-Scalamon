package scalamon.logics.state

import org.scalatest.funsuite.AnyFunSuite

class StatsStateTest extends AnyFunSuite:
  test("test stat modifiers"):
    import scalamon.logics.state.StatsStateModuleImpl.*
    import StatModule.*
    import scalamon.domain.pokemon.Stats

    type StatModifier = Stats => Stats

    val stats = statState(Stats(hp = 10, attack = 6, defense = 3, specialAttack = 4, specialDefense = 2, speed = 6))
    assert(stats.hp == 10)
    assert(stats.attack == 6)

    val weakModifier: StatModifier = _ attack (_ decrease 2)
    val armorModifier: StatModifier = _ defense (_ increase 2)
    val slowModifier: StatModifier = _ speed (_ multiply 0.5)

    val newStats = weakModifier andThen armorModifier andThen slowModifier apply stats

    assert(newStats.hp == 10)
    assert(newStats.attack == 4)
    assert(newStats.defense == 5)
    assert(newStats.speed == 3)