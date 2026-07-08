package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import AlteredStatusUtility.*

class AlteredStatusTest extends org.scalatest.funsuite.AnyFunSuite:

  test(s"Sleep turns should always be between $minSleepTurns and $maxSleepTurns"):
    val turns = (1 to 100).map(_ => getSleepTurns)
    turns.forall(turn => turn >= minSleepTurns && turn <= maxSleepTurns) shouldBe true

  test(s"Confusion turns should always be between $minConfusionTurns and $maxConfusionTurns"):
    val turns = (1 to 100).map(_ => getConfusionTurns)
    turns.forall(turn => turn >= minConfusionTurns && turn <= maxConfusionTurns) shouldBe true

  test("AlteredStatus constants should have correct domain values"):
    statusDamageDivisor shouldBe 8
    paralysisFailureChance shouldBe 25
    confusionSelfHitChance shouldBe 50
    freezeThawingChance shouldBe 10