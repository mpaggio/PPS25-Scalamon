package scalamon.logics.state

import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.moves.AlteredStatus.*
import scalamon.domain.moves.AlteredStatusUtility.*
import scalamon.domain.moves.Accuracy.ProbabilityRoll
import scalamon.logics.state.AlteredStatusModule.*
import scalamon.logics.state.StateTransformerModuleImpl.*

class AlteredStatusModuleTest extends org.scalatest.funsuite.AnyFunSuite with StateFixtures:

  test("Sleeping and Charging Pokemon should not be able to move"):
    import scalamon.domain.moves.Accuracy.given
    Sleeping(3).canMove shouldBe false
    Charging(2).canMove shouldBe false

  test("Paralyzed Pokemon should fail to move when the roll is withing failure chance"):
    given ProbabilityRoll = () => 10
    Paralyzed.canMove shouldBe false

  test("Paralyzed Pokemon should move when the roll is outside failure chance"):
    given ProbabilityRoll = () => 50
    Paralyzed.canMove shouldBe true

  test(s"Frozen Pokemon should move only if it thaws (roll <= $freezeThawingChance)"):
    locally:
      given ProbabilityRoll = () => 5
      Frozen.canMove shouldBe true

    locally:
      given ProbabilityRoll = () => 20
      Frozen.canMove shouldBe false

  test(s"Confused Pokemon should hit itself based on $confusionSelfHitChance"):
    locally:
      given ProbabilityRoll = () => 40
      Confused(2).isSelfHitting shouldBe true

    locally:
      given ProbabilityRoll = () => 60
      Confused(2).isSelfHitting shouldBe false

  test("Burned Pokemon should take damage at the end of turn unless Magic Guard is active"):
    import scalamon.domain.moves.Accuracy.given
    val burnedState = self(active(addStatus(Burned)))(battle)
    val hpBefore = burnedState.self.getActive.currentHp
    val expectedDamage = burnedState.self.getActive.maxHp / statusDamageDivisor

    val afterDamage = Burned.applyCondition(burnedState)
    afterDamage.self.getActive.currentHp shouldBe (hpBefore - expectedDamage)

    val magicGuardState = afterDamage.updateFlags(_.copy(selfMagicGuardActive = true))
    Burned.applyCondition(magicGuardState) shouldBe magicGuardState

  test("Multi-turn statuses should decrement counter or be removed when turns reach 1"):
    import scalamon.domain.moves.Accuracy.given

    val sleep3 = Sleeping(3)
    val stateWithSleep3 = self(active(addStatus(sleep3)))(battle)
    val stateAfterTurn3 = sleep3.applyCondition(stateWithSleep3)
    stateAfterTurn3.self.getActive.statusCondition shouldBe Some(Sleeping(2))

    val sleep1 = Sleeping(1)
    val stateWithSleep1 = self(active(addStatus(sleep1)))(battle)
    val stateAfterTurn1 = sleep1.applyCondition(stateWithSleep1)
    stateAfterTurn1.self.getActive.statusCondition shouldBe Some(Sleeping(0))

    val sleep0 = Sleeping(0)
    val stateWithSleep0 = self(active(addStatus(sleep0)))(battle)
    val stateAfterWakingUp = sleep0.applyCondition(stateWithSleep0)
    stateAfterWakingUp.self.getActive.statusCondition shouldBe None