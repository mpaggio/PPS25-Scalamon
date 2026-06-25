package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import MoveEffectDSL.*
import MoveEffectDSL.Effect.*
import Accuracy.*
import AlteredStatus.*
import scalamon.domain.pokemon.statistics.StatADT.StatKind.*

class MoveEffectDSLTest extends org.scalatest.funsuite.AnyFunSuite:

  test("DSL should create an AlteredState effect with a fluent syntax"):
    val effect: MoveEffect = Effect applying Paralyzed withProbability 10
    effect match
      case AlteredState(factory, probability) =>
        probability shouldBe accuracyFromPercent(10)
        factory() shouldBe Paralyzed
      case _ => fail("Effect was not an AlteredState")

  test("DSL should create a Burn effect"):
    val effect: MoveEffect = Effect applying Burned withProbability 30
    effect match
      case AlteredState(factory, probability) =>
        probability shouldBe accuracyFromPercent(30)
        factory() shouldBe Burned
      case _ => fail("Effect was not an AlteredState")

  test("DSL should create a StatChange effect"):
    val effect: MoveEffect =
      Effect changing SpecialDefense by (-1) withProbability 10
    effect shouldBe StatChange(SpecialDefense, -1, accuracyFromPercent(10))

  test("DSL should create a Heal effect"):
    val effect: MoveEffect =
      Effect healing 50
    effect shouldBe Heal(50)

  test("DSL should create a Recoil effect"):
    val effect: MoveEffect =
      Effect recoil 25
    effect shouldBe Recoil(25)

  test("DSL should create a Recharge effect"):
    val effect: MoveEffect =
      Effect recharging 1
    effect shouldBe Recharge(1)

  test("DSL should create a critical multiplier effect"):
    val effect: MoveEffect =
      Effect multiplyingCriticalBy 8
    effect shouldBe CriticalMultiplier(8)

  test("DSL should create effects using both Int and Double probabilities"):
    val effect1 = Effect applying Paralyzed withProbability 10
    effect1 match
      case AlteredState(factory, probability) =>
        probability shouldBe accuracyFromPercent(10)
        factory() shouldBe Paralyzed
      case _ => fail("Effect was not an AlteredState")
    (Effect changing SpecialDefense by -1 withProbability 25.0) shouldBe 
      StatChange(SpecialDefense, -1, accuracyFromPercent(25))

  test("DSL should reject invalid probabilities"):
    assertThrows[IllegalArgumentException](
      Effect applying Burned withProbability 150)
    assertThrows[IllegalArgumentException](
      Effect changing Attack by 1 withProbability -10)