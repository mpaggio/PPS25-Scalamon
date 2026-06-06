package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import Power.*

class PowerTest extends org.scalatest.funsuite.AnyFunSuite:

  test("Power should be created using only given methods"):
    assertDoesNotCompile("Power(100)")
    assertDoesNotCompile("Power(100.0)")
    val pwr1: Power = fromInt(100)
    pwr1.asString shouldBe "Power: 100"
    val pwr2: Power = fromDouble(100.0)
    pwr2.asString shouldBe "Power: 100"

  test("Power should be created starting from a valid Double"):
    val pwr1: Power = fromDouble(100.0)
    pwr1.asString shouldBe "Power: 100"
    val pwr2: Power = fromDouble(1.0)
    pwr2.asString shouldBe "Power: 1"

  test("Power should be created starting from a valid Int"):
    val pwr1: Power = fromInt(100)
    pwr1.asString shouldBe "Power: 100"
    val pwr2: Power = fromInt(1)
    pwr2.asString shouldBe "Power: 1"

  test("Power should be visualized as String, Int and Double"):
    val pwr1: Power = fromInt(20)
    pwr1.asString shouldBe "Power: 20"
    pwr1.asDouble shouldBe 20.0
    pwr1.asInt shouldBe 20
    val pwr2: Power = fromDouble(20.0)
    pwr2.asString shouldBe "Power: 20"
    pwr2.asDouble shouldBe 20.0
    pwr2.asInt shouldBe 20
