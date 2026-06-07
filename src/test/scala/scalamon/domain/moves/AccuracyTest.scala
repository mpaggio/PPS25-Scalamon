package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import Accuracy.*

class AccuracyTest extends org.scalatest.funsuite.AnyFunSuite:

  test("Accuracy should be created using only given methods"):
    assertDoesNotCompile("Accuracy(100)")
    assertDoesNotCompile("Accuracy(100.0)")
    assertDoesNotCompile("""Accuracy("100.0%")""")
    val acc1: Accuracy = fromPercent(100)
    acc1.asString shouldBe "Accuracy: 100%"
    val acc2: Accuracy = fromRatio(0.0)
    acc2.asString shouldBe "Accuracy: 0%"

  test("Accuracy should be created starting from a valid Double"):
    val acc1: Accuracy = fromRatio(1.0)
    acc1.asString shouldBe "Accuracy: 100%"
    val acc2: Accuracy = fromRatio(0.0)
    acc2.asString shouldBe "Accuracy: 0%"

  test("Accuracy should be created starting from a valid Int"):
    val acc1: Accuracy = fromPercent(100)
    acc1.asString shouldBe "Accuracy: 100%"
    val acc2: Accuracy = fromPercent(0)
    acc2.asString shouldBe "Accuracy: 0%"

  test("Accuracy should be visualized as String, Int and Double"):
    val percent: Accuracy = fromPercent(20)
    percent.asString shouldBe "Accuracy: 20%"
    percent.asDouble shouldBe 20.0
    percent.asInt shouldBe 20
    val ratio: Accuracy = fromRatio(0.2)
    ratio.asString shouldBe "Accuracy: 20%"
    ratio.asDouble shouldBe 20.0
    ratio.asInt shouldBe 20