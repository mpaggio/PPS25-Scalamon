package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.types.Type.*
import DamageMoveCategory.*
import StatusMoveCategory.*
import MoveDSL.*

class MoveDSLTest extends org.scalatest.funsuite.AnyFunSuite:

  test("DSL should create a DamageMove with a fluent syntax"):
    val thunder: DamagingMove = move named "Thunder" withPower 110 withPP 10 withAccuracy 70 withType Electric as Physical
    thunder.name shouldBe "Thunder"
    thunder.power.asInt shouldBe 110
    thunder.pp.asInt shouldBe 10
    thunder.accuracy.asInt shouldBe 70
    thunder.moveType shouldBe Electric
    thunder.category shouldBe Physical

  test("DSL should create a StatusMove with a fluent syntax"):
    val thunder: NonDamagingMove = move named "Thunder wave" withPP 10 withAccuracy 70 withType Electric as Status
    thunder.name shouldBe "Thunder wave"
    thunder.accuracy.asInt shouldBe 70
    thunder.pp.asInt shouldBe 10
    thunder.moveType shouldBe Electric
    thunder.category shouldBe Status

  test("DSL should fail if mandatory fields are missing"):
    assertThrows[IllegalArgumentException](move named "Thunder" withPP 10 withAccuracy 70 withType Electric as Physical)
    assertThrows[IllegalArgumentException](move named "Thunder" withPower 110 withAccuracy 70 withType Electric as Physical)
    assertThrows[IllegalArgumentException](move named "Thunder" withPower 110 withPP 10 withType Electric as Physical)
    assertThrows[IllegalArgumentException](move named "Thunder" withPower 110 withPP 10 withAccuracy 70 as Physical)
    assertThrows[IllegalArgumentException](move named "Thunder" withPower 110 withType Electric as Physical)

  test("DSL should fail if terminal is not used")
    assertDoesNotCompile("""val damageMove: DamageMove = move named "Thunder" withPower 110 withPP 10 withAccuracy 70 withType Electric""")
    assertDoesNotCompile("""val statusMove: StatusMove = move named "Thunder wave" withPP 10 withAccuracy 70 withType Electric""")