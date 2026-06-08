package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import Power.*

class PowerTest extends org.scalatest.funsuite.AnyFunSuite:

  test("Power should be created using only given methods"):
    assertDoesNotCompile("Power(100)")
    assertDoesNotCompile("Power(100.0)")
    val pwr1: Power = powerFromInt(100)
    pwr1.asString shouldBe "Power: 100"
    val pwr2: Power = powerFromDouble(100.0)
    pwr2.asString shouldBe "Power: 100"

  test("Power should be created starting from a valid Double (0.0 < x <= 250.0)"):
    val pwr1: Power = powerFromDouble(250.0)
    pwr1.asString shouldBe "Power: 250"
    val pwr2: Power = powerFromDouble(1.0)
    pwr2.asString shouldBe "Power: 1"
    assertThrows[IllegalArgumentException](powerFromDouble(250.5))
    assertThrows[IllegalArgumentException](powerFromDouble(0.0))
    assertThrows[IllegalArgumentException](powerFromDouble(-0.5))

  test("Power should be created starting from a valid Int (0 < x <= 250)"):
    val pwr1: Power = powerFromInt(250)
    pwr1.asString shouldBe "Power: 250"
    val pwr2: Power = powerFromInt(1)
    pwr2.asString shouldBe "Power: 1"
    assertThrows[IllegalArgumentException](powerFromInt(251))
    assertThrows[IllegalArgumentException](powerFromInt(0))
    assertThrows[IllegalArgumentException](powerFromInt(-1))

  test("Power should be visualized as String, Int and Double"):
    val pwr1: Power = powerFromInt(20)
    pwr1.asString shouldBe "Power: 20"
    pwr1.asDouble shouldBe 20.0
    pwr1.asInt shouldBe 20
    val pwr2: Power = powerFromDouble(20.0)
    pwr2.asString shouldBe "Power: 20"
    pwr2.asDouble shouldBe 20.0
    pwr2.asInt shouldBe 20
