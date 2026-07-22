package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import Accuracy.*

class AccuracyTest extends org.scalatest.funsuite.AnyFunSuite:

  test("Accuracy should be created using only given methods"):
    assertDoesNotCompile("Accuracy(100)")
    assertDoesNotCompile("Accuracy(100.0)")
    assertDoesNotCompile("""Accuracy("100.0%")""")
    val acc1: Accuracy = accuracyFromPercent(100)
    acc1.asString shouldBe "Accuracy: 100%"
    val acc2: Accuracy = accuracyFromRatio(0.0)
    acc2.asString shouldBe "Accuracy: 0%"

  test("Accuracy should be created only starting from a valid ratio, expressed in Double (0.0 <= x <= 1.0)"):
    val acc1: Accuracy = accuracyFromRatio(1.0)
    acc1.asString shouldBe "Accuracy: 100%"
    val acc2: Accuracy = accuracyFromRatio(0.0)
    acc2.asString shouldBe "Accuracy: 0%"
    assertThrows[IllegalArgumentException](accuracyFromRatio(1.5))
    assertThrows[IllegalArgumentException](accuracyFromRatio(-0.5))

  test("Accuracy from ratio should truncate decimal percentages"):
    accuracyFromRatio(0.999).asInt shouldBe 99
    accuracyFromRatio(0.501).asInt shouldBe 50
    accuracyFromRatio(0.009).asInt shouldBe 0

  test("Accuracy should be created only starting from a valid percentage, expressed in Int (0 <= x <= 100)"):
    val acc1: Accuracy = accuracyFromPercent(100)
    acc1.asString shouldBe "Accuracy: 100%"
    val acc2: Accuracy = accuracyFromPercent(0)
    acc2.asString shouldBe "Accuracy: 0%"
    assertThrows[IllegalArgumentException](accuracyFromPercent(150))
    assertThrows[IllegalArgumentException](accuracyFromPercent(-50))

  test("Accuracy should be visualized as String, Int and Double"):
    val percent: Accuracy = accuracyFromPercent(20)
    percent.asString shouldBe "Accuracy: 20%"
    percent.asDouble shouldBe 20.0
    percent.asInt shouldBe 20
    val ratio: Accuracy = accuracyFromRatio(0.2)
    ratio.asString shouldBe "Accuracy: 20%"
    ratio.asDouble shouldBe 20.0
    ratio.asInt shouldBe 20

  test("Accuracy operators should clamp results between 0 and 100"):
    val baseAcc: Accuracy = accuracyFromPercent(80)
    (baseAcc + 30).asInt shouldBe 100
    (baseAcc - 90).asInt shouldBe 0
    (baseAcc * 2.0).asInt shouldBe 100