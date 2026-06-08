package scalamon.logics.state

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalamon.logics.state.StatsStateModuleImpl.*
import scalamon.domain.pokemon.statistics.Stats

class StatsStateTest extends AnyWordSpec with Matchers with StateFixtures:
  "A StatsState" should:
    "correctly store initial stats" in:
      val stats = myPokemon.modifiedStats
      stats.hp.toInt shouldEqual 39
      stats.attack.toInt shouldEqual 52
      stats.defense.toInt shouldEqual 43

    "apply single stat modifier correctly" in:
      val stats = myPokemon.modifiedStats
      val weakModifier: Stats => Stats = _ attack (_ decrease 10)
      val newStats = weakModifier(stats)
      newStats.attack.toInt shouldEqual 42
      newStats.defense.toInt shouldEqual 43

    "apply sequentially composed stat modifiers" in:
      val stats = myPokemon.modifiedStats
      val weakModifier: Stats => Stats = _ attack (_ decrease 2)
      val armorModifier: Stats => Stats = _ defense (_ increase 2)
      val slowModifier: Stats => Stats = _ speed (_ multiply 0.5)
      val newStats = weakModifier andThen armorModifier andThen slowModifier apply stats
      newStats.attack.toInt shouldEqual 50
      newStats.defense.toInt shouldEqual 45
      newStats.speed.toInt shouldEqual 32   // base speed is 65, 65 * 0.5 = 32



