package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.moves.Accuracy.*
import scalamon.domain.alteredStatus.AlteredStatus.*
import scalamon.domain.pokemon.abilities.Target.*
import scalamon.logics.state.StateFixtures
import scalamon.logics.state.StateTransformerModuleImpl.*

class MoveEffectTest extends org.scalatest.funsuite.AnyFunSuite with StateFixtures:

  test("Heal effect should restore HP based on percentage of max HP"):
    val damagedState = self(active(currentHp(_ => 10)))(battle)
    val healEffect = Heal(50)
    val finalState = healEffect.executeEffect(damagedState)
    finalState.self.getActive.currentHp shouldBe 29

  test("Recoil effect should damage the user based on percentage of max HP"):
    val recoilEffect = Recoil(25)
    val finalState = recoilEffect.executeEffect(battle)
    finalState.self.getActive.currentHp shouldBe 30

  test("AlteredState effect should apply status to opponent on successful roll"):
    val alteredStateEffect = AlteredState(() => Paralyzed, accuracyFromPercent(100))
    val finalState = alteredStateEffect.executeEffect(battle)
    finalState.opponent.getActive.statusCondition shouldBe Some(Paralyzed)

  test("AlteredState effect should not apply status to opponent if the roll fails"):
    val alteredStateEffect = AlteredState(() => Poisoned, accuracyFromPercent(0))
    val finalState = alteredStateEffect.executeEffect(battle)
    finalState.opponent.getActive.statusCondition shouldBe None

  test("StateChange effect should modify active Pokemon stats (Self)"):
    val modifier = attack(increase(2))
    val effect = StatChange(modifier, Self, accuracyFromPercent(100))
    val finalState = effect.executeEffect(battle)
    val initialAtk = battle.self.getActive.modifiedStats.attack
    finalState.self.getActive.modifiedStats.attack shouldBe (initialAtk + 2)

  test("StateChange effect should modify active Pokemon stats (Opponent)"):
    val modifier = defense(decrease(1))
    val effect = StatChange(modifier, Opponent, accuracyFromPercent(100))
    val finalState = effect.executeEffect(battle)
    val initialDef = battle.opponent.getActive.modifiedStats.defense
    finalState.opponent.getActive.modifiedStats.defense shouldBe (initialDef - 1)

  test("Recharge effect should add Charging status to the user"):
    val effect = Recharge(1)
    val finalState = effect.executeEffect(battle)
    finalState.self.getActive.statusCondition shouldBe Some(Charging(1))

  test("ComposableEffect should execute its internal transformer"):
    val transformer = self(active(takeDamage(5))).andThen(self(active(takeDamage(3))))
    val effect = ComposableEffect(transformer)
    val finalState = effect.executeEffect(battle)
    finalState.self.getActive.currentHp shouldBe (battle.self.getActive.currentHp - 8)

  test("CriticalMultiplier effect should currently return the state unchanged"):
    val effect = CriticalMultiplier(2)
    effect.executeEffect(battle) shouldBe battle