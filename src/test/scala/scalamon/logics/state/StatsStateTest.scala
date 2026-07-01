package scalamon.logics.state

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalamon.logics.state.StatsStateModuleImpl.{decrease, *}
import scalamon.domain.pokemon.statistics.Stats

class StatsStateTest extends AnyWordSpec with Matchers with StateFixtures:
  
  type StatModifier = StatsState => StatsState
  
  "A StatsState" should:
    "correctly store initial stats" in:
      val stats = myPokemon.modifiedStats
      stats.hp shouldEqual 39
      stats.attack shouldEqual 52
      stats.defense shouldEqual 43

    "apply single stat modifier correctly" in:
      val stats = myPokemon.modifiedStats
      val weakModifier: StatModifier = attack(decrease(10))
      val newStats = weakModifier(stats)
      newStats.attack shouldEqual 42
      newStats.defense shouldEqual 43

    "apply sequentially composed stat modifiers" in:
      val stats = myPokemon.modifiedStats
      val weakModifier: StatModifier = attack(decrease(2))
      val armorModifier: StatModifier = defense(increase(2))
      val slowModifier: StatModifier = speed(multiply(0.5))
      val newStats = weakModifier andThen armorModifier andThen slowModifier apply stats
      newStats.attack shouldEqual 50
      newStats.defense shouldEqual 45
      newStats.speed shouldEqual 32   // base speed is 65, 65 * 0.5 = 32



