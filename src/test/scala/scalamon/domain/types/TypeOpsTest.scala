package scalamon.domain.types

import org.scalatest.funsuite.AnyFunSuite
import TypeEffectiveness.*

class TypeOpsTest extends AnyFunSuite:
  import Type.*

  private val SuperEffectiveMultiplier = 2.0

  test("effectivenessAgainst returns SuperEffective for Fire against Grass") {
    assert(Fire.effectivenessAgainst(Grass) == SuperEffective)
  }

  test("effectivenessAgainst returns NotVeryEffective for Fire against Water") {
    assert(Fire.effectivenessAgainst(Water) == NotVeryEffective)
  }

  test("multiplierAgaist returns the correct damage multiplier for Fire against Grass") {
    assert(Fire.multiplierAgainst(Grass) == SuperEffectiveMultiplier)
  }

  test("isSuperEffectiveAgainst returns true for Psychic against Poison") {
    assert(Psychic.isSuperEffectiveAgainst(Poison))
  }

  test("isNotVeryEffectiveAgainst returns true for Grass against Poison") {
    assert(Grass.isNotVeryEffectiveAgainst(Poison))
  }

  test("hasNoEffect returns false when there is no immunity") {
    assert(!Fire.hasNoEffectAgainst(Grass))
  }

  test("unspecified matchup is neutral") {
    assert(Poison.effectivenessAgainst(Water) === Neutral)
  }
  