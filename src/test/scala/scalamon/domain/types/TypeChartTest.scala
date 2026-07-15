package scalamon.domain.types

import org.scalatest.funsuite.AnyFunSuite

class TypeChartTest extends AnyFunSuite:
  import Type.*
  import TypeEffectiveness.*

  test("Fire is supereffective against Grass") {
    assert(TypeChart.effectiveness(Fire, Grass) == SuperEffective)
  }

  test("Fire is not very effective against Water") {
    assert(TypeChart.effectiveness(Fire, Water) == NotVeryEffective)
  }

  test("Water is super effective against Fire") {
    assert(TypeChart.effectiveness(Water, Fire) == SuperEffective)
  }

  test("Grass is not very effective against Poison") {
    assert(TypeChart.effectiveness(Grass, Poison) == NotVeryEffective)
  }

  test("Electric is super effective against Water") {
    assert(TypeChart.effectiveness(Electric, Water) == SuperEffective)
  }

  test("Psychic is super effective against Poison") {
    assert(TypeChart.effectiveness(Psychic, Poison) == SuperEffective)
  }

  test("Poison is super effective against Grass") {
    assert(TypeChart.effectiveness(Poison, Grass) == SuperEffective)
  }

  test("Unspecified matchups default to Neutral") {
    assert(TypeChart.effectiveness(Fire, Psychic) == Neutral)
  }

  test("Another unspecified matchup defaults to Neutral") {
    assert(TypeChart.effectiveness(Poison, Water) == Neutral)
  }
