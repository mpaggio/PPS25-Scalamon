package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import PowerPoints.*

class PowerPointsTest extends org.scalatest.funsuite.AnyFunSuite:

  test("Power points should be created using only given methods"):
    assertDoesNotCompile("PP(30)")
    assertDoesNotCompile("PP(30.0)")
    val pp1: PP = powerPointsFromInt(30)
    pp1.asString shouldBe "PP: 30"
    val pp2: PP = powerPointsFromDouble(30.0)
    pp2.asString shouldBe "PP: 30"

  test("Power points should be created starting from a valid Double (0.0 < x <= 64.0)"):
    val pp1: PP = powerPointsFromDouble(30.0)
    pp1.asString shouldBe "PP: 30"
    val pp2: PP = powerPointsFromDouble(30.0)
    pp2.asString shouldBe "PP: 30"
    assertThrows[IllegalArgumentException](powerPointsFromDouble(64.1))
    assertThrows[IllegalArgumentException](powerPointsFromDouble(0.0))
    assertThrows[IllegalArgumentException](powerPointsFromDouble(-1.0))

  test("Power points should be created starting from a valid Int (0 < x <= 64)"):
    val pp1: PP = powerPointsFromInt(64)
    pp1.asString shouldBe "PP: 64"
    val pp2: PP = powerPointsFromInt(1)
    pp2.asString shouldBe "PP: 1"
    assertThrows[IllegalArgumentException](powerPointsFromInt(65))
    assertThrows[IllegalArgumentException](powerPointsFromInt(0))
    assertThrows[IllegalArgumentException](powerPointsFromInt(-1))

  test("Power points should be visualized as String, Int and Double"):
    val pp1: PP = powerPointsFromInt(20)
    pp1.asString shouldBe "PP: 20"
    pp1.asDouble shouldBe 20.0
    pp1.asInt shouldBe 20
    val pp2: PP = powerPointsFromDouble(20.0)
    pp2.asString shouldBe "PP: 20"
    pp2.asDouble shouldBe 20.0
    pp2.asInt shouldBe 20