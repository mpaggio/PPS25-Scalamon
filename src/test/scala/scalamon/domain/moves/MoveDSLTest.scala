package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.types.Type.*
import DamageMoveCategory.*
import StatusMoveCategory.*
import MoveDSL.*
import MoveEffectDSL.*
import MoveEffectDSL.Effect.*
import MoveEffect.*
import scalamon.domain.pokemon.statistics.StatADT.StatKind.*

class MoveDSLTest extends org.scalatest.funsuite.AnyFunSuite:

  test("DSL should create a DamageMove without a side effect with a fluent syntax"):
    val thunder: DamagingMove = move named "Thunder" withPower 110 withPP 10 withAccuracy 70 withType Electric as Physical
    thunder.name shouldBe "Thunder"
    thunder.power.asInt shouldBe 110
    thunder.pp.asInt shouldBe 10
    thunder.accuracy.asInt shouldBe 70
    thunder.moveType shouldBe Electric
    thunder.category shouldBe Physical

  test("DSL should create a DamageMove with a side effect with a fluent syntax"):
    val thunder: DamagingMove = move named "Thunder" withPower 110 withPP 10 withAccuracy 70 withType Electric withEffect (Effect applying Paralysis withProbability 10) as Special
    thunder.name shouldBe "Thunder"
    thunder.power.asInt shouldBe 110
    thunder.pp.asInt shouldBe 10
    thunder.accuracy.asInt shouldBe 70
    thunder.moveType shouldBe Electric
    thunder.category shouldBe Special
    thunder.effect shouldBe defined

  test("DSL should create a StatusMove with a side effect with a fluent syntax"):
    val thunder: NonDamagingMove = move named "Thunder wave" withPP 10 withAccuracy 70 withType Electric withEffect (Effect healing 50) as Status
    thunder.name shouldBe "Thunder wave"
    thunder.accuracy.asInt shouldBe 70
    thunder.pp.asInt shouldBe 10
    thunder.moveType shouldBe Electric
    thunder.category shouldBe Status

  test("DSL should correctly chain complex effect like StatChanges"):
    val psychic = move named "Psychic" withPower 90 withPP 16 withAccuracy 100 withType Psychic withEffect (Effect changing SpecialDefense by -1 withProbability 10) as Special
    psychic.effect.get shouldBe a [StatChange]

  test("DSL should fail if mandatory fields are missing"):
    assertThrows[IllegalArgumentException](move named "Thunder" withPP 10 withAccuracy 70 withType Electric as Physical)
    assertThrows[IllegalArgumentException](move named "Thunder" withPower 110 withAccuracy 70 withType Electric as Physical)
    assertThrows[IllegalArgumentException](move named "Thunder" withPower 110 withPP 10 withType Electric as Physical)
    assertThrows[IllegalArgumentException](move named "Thunder" withPower 110 withPP 10 withAccuracy 70 as Physical)
    assertThrows[IllegalArgumentException](move named "Thunder" withPower 110 withType Electric as Physical)
    assertThrows[IllegalArgumentException](move named "Thunder wave" withAccuracy 70 withType Electric withEffect (Effect healing 50) as Status)
    assertThrows[IllegalArgumentException](move named "Thunder wave" withPP 10 withType Electric withEffect (Effect healing 50) as Status)
    assertThrows[IllegalArgumentException](move named "Thunder wave" withPP 10 withAccuracy 70 withEffect (Effect healing 50) as Status)
    assertThrows[IllegalArgumentException](move named "Thunder wave" withPP 10 withAccuracy 70 withType Electric as Status)

  test("DSL should fail if terminal is not used")
    assertDoesNotCompile("""val damageMove: DamageMove = move named "Thunder" withPower 110 withPP 10 withAccuracy 70 withType Electric""")
    assertDoesNotCompile("""val statusMove: StatusMove = move named "Thunder wave" withPP 10 withAccuracy 70 withType Electric""")