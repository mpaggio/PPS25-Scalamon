package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import MoveEffectDSL.*
import MoveEffectDSL.Effect.*
import MoveEffect.*
import Accuracy.*
import scalamon.domain.pokemon.statistics.StatADT.StatKind.*

class MoveEffectDSLTest extends org.scalatest.funsuite.AnyFunSuite:

  test("DSL should create an AlteredState effect with a fluent syntax"):
    val effect: MoveEffect = Effect applying Paralysis withProbability 10
    effect shouldBe AlteredState(Paralysis, accuracyFromPercent(10))

  test("DSL should create a Burn effect"):
    val effect: MoveEffect = Effect applying Burn withProbability 30
    effect shouldBe AlteredState(Burn, accuracyFromPercent(30))

  test("DSL should create a StatChange effect"):
    val effect: MoveEffect = Effect changing SpecialDefense by (-1) withProbability 10
    effect shouldBe StatChange(SpecialDefense, -1, accuracyFromPercent(10))

  test("DSL should create a Heal effect"):
    val effect: MoveEffect = Effect healing 50
    effect shouldBe Heal(50)

  test("DSL should create a Recoil effect"):
    val effect: MoveEffect = Effect recoil 25
    effect shouldBe Recoil(25)

  test("DSL should create a Recharge effect"):
    val effect: MoveEffect = Effect recharging 1
    effect shouldBe Recharge(1)