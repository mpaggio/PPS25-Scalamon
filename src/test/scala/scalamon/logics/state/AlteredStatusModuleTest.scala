package scalamon.logics.state

import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.alteredStatus.AlteredStatus.*
import scalamon.domain.alteredStatus.AlteredStatusUtility.*
import scalamon.domain.moves.Accuracy.ProbabilityRoll
import scalamon.domain.types.Type.*
import scalamon.domain.alteredStatus.AlteredStatusModule.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.domain.weather.Weather.*
import scalamon.logics.weather.WeatherSystem.given

class AlteredStatusModuleTest extends org.scalatest.funsuite.AnyFunSuite with StateFixtures:

  test("Sleeping and Charging Pokemon should not be able to move"):
    import scalamon.domain.moves.Accuracy.given
    import scalamon.logics.weather.WeatherSystem.given
    Sleeping(3).canMove(Grass, ClearSky) shouldBe false
    Charging(2).canMove(Grass, ClearSky) shouldBe false

  test("Paralyzed Pokemon should fail to move when the roll is withing failure chance"):
    given ProbabilityRoll = () => 10
    Paralyzed.canMove(Grass, ClearSky) shouldBe false

  test("Paralyzed Pokemon should move when the roll is outside failure chance"):
    given ProbabilityRoll = () => 50
    Paralyzed.canMove(Grass, ClearSky) shouldBe true

  test("Thunderstorm should override paralysis failure chance for Electric type"):
    locally:
      given ProbabilityRoll = () => 60
      Paralyzed.canMove(Electric, Thunderstorm) shouldBe false

      locally:
        given ProbabilityRoll = () => 80
        Paralyzed.canMove(Electric, Thunderstorm) shouldBe true

  test(s"Frozen Pokemon should move only if it thaws (roll <= $freezeThawingChance)"):
    locally:
      given ProbabilityRoll = () => 5
      Frozen.canMove(Grass, ClearSky) shouldBe true

    locally:
      given ProbabilityRoll = () => 20
      Frozen.canMove(Grass, ClearSky) shouldBe false

  test("Heavy sunlight should prevent frozen pokemon from thawing"):
    given ProbabilityRoll = () => 1
    Frozen.canMove(Grass, HeavySunlight) shouldBe false

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

    val magicGuardState = self(updateFlags(_.copy(magicGuardActive = true)))(afterDamage)
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

  test("Heavy sunlight should increase poison residual damage"):
    import scalamon.domain.moves.Accuracy.given

    val poisonedState = self(active(addStatus(Poisoned)))(battle)
    val hpBefore = poisonedState.self.getActive.currentHp
    val baseDamage = poisonedState.self.getActive.maxHp / statusDamageDivisor
    val normalDamageState = Poisoned.applyCondition(poisonedState.copy(weather = ClearSky))
    val weatherDamageState = Poisoned.applyCondition(poisonedState.copy(weather = HeavySunlight))
    val normalLoss = hpBefore - normalDamageState.self.getActive.currentHp
    val weatherLoss = hpBefore - weatherDamageState.self.getActive.currentHp
    normalLoss shouldBe baseDamage
    weatherLoss shouldBe (baseDamage * 1.5).toInt
    weatherLoss should be > normalLoss