package scalamon.domain.pokemon.abilities

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.moves.AlteredStatus.*
import scalamon.domain.moves.DamageMove
import scalamon.domain.moves.DamageMoveCategory.Physical
import scalamon.domain.moves.MoveDatabase.{allMoves, ofType}
import scalamon.domain.pokemon.abilities.Ability.*
import scalamon.domain.pokemon.abilities.AbilityTrigger.*
import scalamon.domain.types.Type.*
import scalamon.domain.weather.Weather
import scalamon.domain.weather.Weather.*
import scalamon.logics.state.BattleStateImpl.{BattleState, battleState}
import scalamon.logics.state.StateFixtures

class MyAbilityBookTest extends AnyFunSuite with StateFixtures:
  // HELPERS

  private def run(ability: Ability, trigger: AbilityTrigger)(s: BattleState): BattleState =
    MyAbilityBook.runTrigger(trigger, AbilitySlot(primary = ability))(s)

  private def withDamagedSelf(dmg: Int = 10): BattleState =
    battle self (_ active (_ takeDamage dmg))

  private def withWeather(w: Weather): BattleState =
    battle.setWeather(w)

  private def withWeatherAndDamage(w: Weather, dmg: Int = 10): BattleState =
    withDamagedSelf(dmg).setWeather(w)

  private def selfHp(s: BattleState): Int = s.self.getActive.currentHp.toInt

  private def enemyHp(s: BattleState): Int = s.opponent.getActive.currentHp.toInt

  private def maxSelfHp(s: BattleState): Int = s.self.getActive.maxHp

  private def maxEnemyHp(s: BattleState): Int = s.opponent.getActive.maxHp

  private def withSelfAbility(ability: Ability): BattleState =
    battle self (_ active (p => p.copy(species = p.species.copy(
      abilitySlot = AbilitySlot(primary = ability)
    ))))

  private val fireMoveOpt = allMoves.collectFirst { case m: DamageMove if m.moveType == Fire => m }
  private val waterMoveOpt = allMoves.collectFirst { case m: DamageMove if m.moveType == Water => m }
  private val electricMoveOpt = allMoves.collectFirst { case m: DamageMove if m.moveType == Electric => m }

  // FIRE

  test("Blaze gives 1.5x multiplier to Fire type moves when self HP <= 1/3"):
      val lowHpState = battle self (_ active (_ takeDamage 30))
      fireMoveOpt.foreach: fireMove =>
        AbilityDamageModifier.attackerMultiplier(lowHpState, fireMove) shouldEqual 1.5

  test("Blaze gives no multiplier on Fire move when self HP > 1/3"):
    fireMoveOpt.foreach: fireMove =>
      AbilityDamageModifier.attackerMultiplier(battle, fireMove) shouldEqual 1.0

  test("SolarScales heals self by maxHp/16 when weather is HeavySunLight"):
    val s = withWeatherAndDamage(HeavySunlight)
    selfHp(run(SolarScales, OnTurnEnd)(s)) shouldEqual selfHp(s) + maxSelfHp(s) / 16

  test("SolarScales does not heal when weather is not HeavySunlight"):
    run(SolarScales, OnTurnEnd)(battle).self.getActive.currentHp shouldEqual battle.self.getActive.currentHp

  test("SolarPower damages self by maxHp/16 when weather is HeavySunLight"):
    val s = withWeather(HeavySunlight)
    selfHp(run(SolarPower, OnTurnEnd)(s)) shouldEqual( selfHp(s) - maxSelfHp(s)/ 16)

  test("SolarPower does not damage when weather is not HeavySunlight"):
    run(SolarPower, OnTurnEnd)(battle).self.getActive.currentHp shouldEqual
      battle.self.getActive.currentHp

  test("Drought sets weather to HeavySunlight when current weather is ClearSky"):
    val s = withWeather(ClearSky)
    run(Drought, OnSwitchIn)(s).weather shouldEqual HeavySunlight

  test("Drought does not change weather when is already non-ClearSky"):
    val s = withWeather(Rain)
    run(Drought, OnSwitchIn)(s).weather shouldEqual Rain

  test("FlashFire activates flag when last opponent move is Fire Type"):
    fireMoveOpt.foreach: fireMove =>
      val s = battle.updateFlags(_.copy(lastOpponentMove = fireMoveOpt))
      run(FlashFire, OnDamageTaken)(s).flags.selfFlashFireActive shouldBe true

  test("FlashFire does not activate flag when last opponent move is not Fire type"):
    waterMoveOpt.foreach: waterMove =>
      val s = battle.updateFlags(_.copy(lastOpponentMove = waterMoveOpt))
      run(FlashFire, OnDamageTaken)(s).flags.selfFlashFireActive shouldBe false

  test("FlashFire does not activate flag when ther is no last opponent move"):
    run(FlashFire, OnDamageTaken)(battle).flags.selfFlashFireActive shouldBe false

  test("FlameBody may burn opponent when hit (10 probabilistic trials"):
    val results = (1 to 10).map ( _ => run(FlameBody, OnDamageTaken)(battle))
    results.exists(_.opponent.getActive.statusCondition.contains(Burned)) shouldBe true
    results.exists(_.opponent.getActive.statusCondition.isEmpty) shouldBe true

  test("Guts gives 1.3x on Physical when self has a status"):
    val physMove = allMoves.collectFirst { case m: DamageMove if m.category == Physical => m }
    physMove.foreach: move =>
      val s = withSelfAbility(Guts) self (_ active (_ addStatus Paralyzed))
      AbilityDamageModifier.attackerMultiplier(s, move) shouldEqual 1.3

  test("Guts gives no multiplier without status"):
    val physMove = allMoves.collectFirst { case m: DamageMove if m.category == Physical => m }
    physMove.foreach: move =>
      val s = withSelfAbility(Guts)
      AbilityDamageModifier.attackerMultiplier(s, move) shouldEqual 1.0

  // WATER

  test("Torrent gives 1.5x multiplier to Water type moves when self HP <= 1/3"):
    val lowHpState = withSelfAbility(Torrent) self (_ active (_ takeDamage 30))
    waterMoveOpt.foreach: waterMove =>
      AbilityDamageModifier.attackerMultiplier(lowHpState, waterMove) shouldEqual 1.5

  test("RainDish heals self by maxHp/16 when weather is Rain"):
    val s = withWeatherAndDamage(Rain)
    selfHp(run(RainDish, OnTurnEnd)(s)) shouldEqual selfHp(s) + maxSelfHp(s) / 16

  test("RainDish does not heal when weather is not Rain"):
    run(RainDish, OnTurnEnd)(battle).self.getActive.currentHp shouldEqual battle.self.getActive.currentHp

  test("WaterAbsorb heals self by maxHp/4 when hit by Water type move"):
    waterMoveOpt.foreach: waterMove =>
      val damaged = battle self (_ active (_ takeDamage 20)) // prima togli HP
      val s = damaged.updateFlags(_.copy(lastOpponentMove = Some(waterMove)))
      val result = run(WaterAbsorb, OnDamageTaken)(s)
      result.self.getActive.currentHp shouldEqual s.self.getActive.currentHp + s.self.getActive.maxHp / 4

  test("WaterAbsorb does not heal when hit by non-Water move"):
    fireMoveOpt.foreach: fireMove =>
      val s = battle.updateFlags(_.copy(lastOpponentMove = Some(fireMove)))
      run(WaterAbsorb, OnDamageTaken)(s).self.getActive.currentHp shouldEqual battle.self.getActive.currentHp

  test("Hydration clears status condition when weather is Rain"):
    val s = (battle self (_ active (_ addStatus Burned))).setWeather(Rain)
    s.self.getActive.statusCondition shouldBe defined
    run(Hydration, OnTurnStart)(s).self.getActive.statusCondition shouldBe empty

  test("Hydration does not clear status condition when weather is not Rain"):
    val s = (battle self (_ active (_ addStatus Burned)))
    run(Hydration, OnTurnStart)(s).self.getActive.statusCondition shouldBe defined

  test("Intimidate reduces opponent's active attack by 10%"):
    val before = battle.opponent.getActive.modifiedStats.attack
    val after = run(Intimidate, OnSwitchIn)(battle).opponent.getActive.modifiedStats.attack
    // testa il valore esatto prodotto da multiply, non una stima
    after shouldEqual (before * 0.9).toInt

  test("Moxie boosts self active attack by 10% after KO"):
    val before = battle.self.getActive.modifiedStats.attack
    val after = run(Moxie, OnKODealt)(battle).self.getActive.modifiedStats.attack
    after shouldEqual (before * 1.1).toInt
  // GRASS

