package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.types.Type.*
import DamageMoveCategory.*
import StatusMoveCategory.*
import MoveDSL.*
import MoveEffectDSL.*
import MoveEffectDSL.Effect.*
import Accuracy.*
import AlteredStatus.*
import scalamon.logics.state.StatsStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.Modifier

class MoveDSLTest extends org.scalatest.funsuite.AnyFunSuite:

  def shouldFail(expr: => Any): Unit =
    assertThrows[IllegalArgumentException](expr)

  test("DSL should create a DamageMove without a side effect with a fluent syntax"):
    val thunder: DamagingMove = move
      .named("Thunder")
      .withPower(110)
      .withPP(10)
      .withAccuracy(70)
      .withType(Electric)
      .as(Physical)
    thunder.name shouldBe "Thunder"
    thunder.power.asInt shouldBe 110
    thunder.pp.asInt shouldBe 10
    thunder.accuracy.asInt shouldBe 70
    thunder.moveType shouldBe Electric
    thunder.category shouldBe Physical

  test("DSL should create a DamageMove with a side effect with a fluent syntax"):
    val thunder: DamagingMove = move
      .named("Thunder")
      .withPower(110)
      .withPP(10)
      .withAccuracy(70)
      .withType(Electric)
      .withEffect(Effect applying Paralyzed withProbability 10)
      .as(Special)
    thunder.name shouldBe "Thunder"
    thunder.power.asInt shouldBe 110
    thunder.pp.asInt shouldBe 10
    thunder.accuracy.asInt shouldBe 70
    thunder.moveType shouldBe Electric
    thunder.category shouldBe Special
    thunder.effect shouldBe defined
    val effect = thunder.effect.get.asInstanceOf[AlteredState]
    effect.probability shouldBe accuracyFromPercent(10)
    effect.statusFactory() shouldBe Paralyzed

  test("DSL should create a StatusMove with a side effect with a fluent syntax"):
    val thunder: NonDamagingMove = move
      .named("Thunder wave")
      .withPP(10)
      .withAccuracy(70)
      .withType(Electric)
      .withEffect(Effect healing 50)
      .as(Status)
    thunder.name shouldBe "Thunder wave"
    thunder.accuracy.asInt shouldBe 70
    thunder.pp.asInt shouldBe 10
    thunder.moveType shouldBe Electric
    thunder.category shouldBe Status
    thunder.effect shouldBe Heal(50)

  test("DSL should correctly chain complex effect like StatChanges"):
    val modifier: Modifier = _ specialDefense (_ decrease 1)
    val psychic = move
      .named("Psychic")
      .withPower(90)
      .withPP(16)
      .withAccuracy(100)
      .withType(Psychic)
      .withEffect(Effect changing modifier withProbability 10)
      .as(Special)
    psychic.effect shouldBe Some(StatChange(modifier, accuracyFromPercent(10)))

  test("DSL should fail if mandatory fields are missing"):
    shouldFail(
      move named "Thunder" withPP 10 withAccuracy 70 withType Electric as Physical)
    shouldFail(
      move named "Thunder" withPower 110 withAccuracy 70 withType Electric as Physical)
    shouldFail(
      move named "Thunder" withPower 110 withPP 10 withType Electric as Physical)
    shouldFail(
      move named "Thunder" withPower 110 withPP 10 withAccuracy 70 as Physical)
    shouldFail(
      move named "Thunder" withPower 110 withType Electric as Physical)
    shouldFail(
      move named "Thunder wave" withAccuracy 70 withType Electric withEffect (Effect healing 50) as Status)
    shouldFail(
      move named "Thunder wave" withPP 10 withType Electric withEffect (Effect healing 50) as Status)
    shouldFail(
      move named "Thunder wave" withPP 10 withAccuracy 70 withEffect (Effect healing 50) as Status)
    shouldFail(
      move named "Thunder wave" withPP 10 withAccuracy 70 withType Electric as Status)

  test("DSL should fail if terminal is not used")
    assertDoesNotCompile(
      """val damageMove: DamageMove =
        |move named "Thunder"
        |withPower 110
        |withPP 10
        |withAccuracy 70
        |withType Electric""".stripMargin)
    assertDoesNotCompile(
      """val statusMove: StatusMove =
        |move named "Thunder wave"
        |withPP 10
        |withAccuracy 70
        |withType Electric""".stripMargin)