package scalamon.domain.pokemon.abilities

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.moves.AlteredStatus.*
import scalamon.domain.moves.{DamageMove, DamageMoveCategory, Move}
import scalamon.domain.moves.DamageMoveCategory.Physical
import scalamon.domain.moves.MoveDatabase.allMoves
import scalamon.domain.pokemon.abilities.Ability.*
import scalamon.domain.pokemon.abilities.AbilityTrigger.*
import scalamon.domain.pokemon.abilities.Target.*
import scalamon.domain.types.Type.*
import scalamon.domain.weather.Weather
import scalamon.domain.weather.Weather.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.state.StateFixtures

class MyAbilityBookTest extends AnyFunSuite with StateFixtures:
  // HELPERS

  /**
   * Runs the given ability trigger on the provided battle state.
   * @param ability The ability to be triggered.
   * @param trigger the specific trigger event for the ability.
   * @param s the current battle state before the ability is triggered.
   * @return the new battle state after the ability has been triggered.
   */
  private def run(ability: Ability, trigger: AbilityTrigger)(s: BattleState): BattleState =
    MyAbilityBook.runTrigger(trigger, AbilitySlot(primary = ability))(s)

  /**
   * Damages the self active Pokémon in the battle state by a specified amount.
   * @param dmg the damage applied
   * @return the new battle state after the self active Pokémon has taken damage.
   */
  private def withDamagedSelf(dmg: Int = 10): BattleState =
    self(active(takeDamage(dmg)))(battle)

  /**
   * Applies the specified weather condition to the battle state.
   * @param w the weather condition to be set
   * @return the new battle state after the weather has been set.
   */
  private def withWeather(w: Weather): BattleState =
    setWeather(w)(battle)

  /**
   * Applies the specified weather condition and damages the self active Pokémon in the battle state by a specified amount.
   * @param w the weather condition to be set
   * @param dmg the damage applied to the self active Pokémon
   * @return the new battle state after the weather has been set and the self active Pokémon has taken damage.
   */
  private def withWeatherAndDamage(w: Weather, dmg: Int = 10): BattleState =
    setWeather(w)(withDamagedSelf(dmg))

  private def selfHp(s: BattleState): Int = s.self.getActive.currentHp

  private def enemyHp(s: BattleState): Int = s.opponent.getActive.currentHp

  private def maxSelfHp(s: BattleState): Int = s.self.getActive.maxHp

  private def maxEnemyHp(s: BattleState): Int = s.opponent.getActive.maxHp

  /**
   * Applies the specified ability to the self active Pokémon in the battle state.
   * @param ability the ability to be set
   * @return the new battle state after the self active Pokémon's ability has been set.
   */
  private def withSelfAbility(ability: Ability): BattleState =
    self(active(p => p.copy(species = p.species.copy(
      abilitySlot = AbilitySlot(primary = ability)
    ))))(battle)

  /**
   * Applies the specified ability to the opponent active Pokémon in the battle state.
   * @param ability the ability to be set
   * @return the new battle state after the opponent active Pokémon's ability has been set.
   */
  private def withOpponentAbility(ability: Ability): BattleState =
    opponent(active(p => p.copy(species = p.species.copy(
      abilitySlot = AbilitySlot(primary = ability)
    ))))(battle)

  /**
   * Adds the specified move to the opponent active Pokémon's moves in the battle state.
   * @param move the move to be added
   * @return the new battle state after the opponent active Pokémon's moves have been updated.
   */
  private def withOpponentMove(move: Move): BattleState =
    opponent(active(p => p.copy(
      moves = p.moves + (move.name -> moveInitialState(move))
    )))(battle)

  private val fireMoveOpt = allMoves.collectFirst { case m: DamageMove if m.moveType == Fire => m }
  private val waterMoveOpt = allMoves.collectFirst { case m: DamageMove if m.moveType == Water => m }
  private val grassMoveOpt = allMoves.collectFirst { case m: DamageMove if m.moveType == Grass => m }
  private val electricMoveOpt = allMoves.collectFirst { case m: DamageMove if m.moveType == Electric => m }
  private val physMoveOpt = allMoves.collectFirst { case m: DamageMove if m.category == Physical => m }

  // FIRE

  test("Blaze gives 1.5x multiplier to Fire type moves when self HP <= 1/3") {
    val lowHpState = self(active(takeDamage(30)))(battle)
    fireMoveOpt.foreach: fireMove =>
      AbilityDamageModifier.attackerMultiplier(lowHpState, fireMove) shouldEqual 1.5
  }

  test("Blaze gives no multiplier on Fire move when self HP > 1/3") {
    fireMoveOpt.foreach: fireMove =>
      AbilityDamageModifier.attackerMultiplier(battle, fireMove) shouldEqual 1.0
  }

  test("SolarScales heals self by maxHp/16 when weather is HeavySunLight") {
    val s = withWeatherAndDamage(HeavySunlight)
    selfHp(run(SolarScales, OnTurnEnd)(s)) shouldEqual selfHp(s) + maxSelfHp(s) / 16
  }

  test("SolarScales does not heal when weather is not HeavySunlight") {
    run(SolarScales, OnTurnEnd)(battle).self.getActive.currentHp shouldEqual battle.self.getActive.currentHp
  }

  test("SolarPower damages self by maxHp/16 when weather is HeavySunLight") {
    val s = withWeather(HeavySunlight)
    selfHp(run(SolarPower, OnTurnEnd)(s)) shouldEqual (selfHp(s) - maxSelfHp(s) / 16)
  }

  test("SolarPower gives 1.3x multiplier to Special moves in HeavySunlight") {
    val specialMoveOpt = allMoves.collectFirst { case m: DamageMove if m.category == DamageMoveCategory.Special => m }
    specialMoveOpt.foreach: specialMove =>
      val s = setWeather(HeavySunlight)(withSelfAbility(SolarPower))
      AbilityDamageModifier.attackerMultiplier(s, specialMove) shouldEqual 1.3
  }

  test("SolarPower does not damage when weather is not HeavySunlight") {
    run(SolarPower, OnTurnEnd)(battle).self.getActive.currentHp shouldEqual
      battle.self.getActive.currentHp
  }

  test("Drought sets weather to HeavySunlight when current weather is ClearSky") {
    val s = withWeather(ClearSky)
    run(Drought, OnSwitchIn(Self))(s).weather shouldEqual HeavySunlight
  }

  test("Drought does not change weather when is already non-ClearSky") {
    val s = withWeather(Rain)
    run(Drought, OnSwitchIn(Self))(s).weather shouldEqual Rain
  }

  test("DroughtAura gives 1.1x multiplier to Fire type moves") {
    fireMoveOpt.foreach: fireMove =>
      val s = withSelfAbility(DroughtAura)
      AbilityDamageModifier.attackerMultiplier(s, fireMove) shouldEqual 1.1
  }

  test("DroughtAura gives no multiplier to non-Fire type moves") {
    waterMoveOpt.foreach: waterMove =>
      val s = withSelfAbility(DroughtAura)
      AbilityDamageModifier.attackerMultiplier(s, waterMove) shouldEqual 1.0
  }

  test("FlashFire activates flag when last opponent move is Fire Type") {
    fireMoveOpt.foreach: fireMove =>
      val s = self(_.updateFlags(_.copy(lastOpponentMove = fireMoveOpt)))(battle)
      run(FlashFire, OnDamageTaken(Self))(s).self.flags.flashFireActive shouldBe true
  }

  test("FlashFire does not activate flag when last opponent move is not Fire type") {
    waterMoveOpt.foreach: waterMove =>
      val s = self(_.updateFlags(_.copy(lastOpponentMove = waterMoveOpt)))(battle)
      run(FlashFire, OnDamageTaken(Self))(s).self.flags.flashFireActive shouldBe false
  }

  test("FlashFire does not activate flag when there is no last opponent move") {
    run(FlashFire, OnDamageTaken(Self))(battle).self.flags.flashFireActive shouldBe false
  }

  test("FlashFire gives 1.3x multiplier to Fire type moves when flag is active") {
    fireMoveOpt.foreach: fireMove =>
      val s = self(_.updateFlags(_.copy(flashFireActive = true)))(withSelfAbility(FlashFire))
      AbilityDamageModifier.attackerMultiplier(s, fireMove) shouldEqual 1.3
  }

  test("FlameBody may burn opponent when hit (10 probabilistic trials") {
    val results = (1 to 50).map(_ => run(FlameBody, OnDamageTaken(Self))(battle))
    results.exists(_.opponent.getActive.statusCondition.contains(Burned)) shouldBe true
    results.exists(_.opponent.getActive.statusCondition.isEmpty) shouldBe true
  }

  test("Guts gives 1.3x on Physical when self has a status") {
    val physMove = allMoves.collectFirst { case m: DamageMove if m.category == Physical => m }
    physMove.foreach: move =>
      val s = self(active(addStatus(Paralyzed)))(withSelfAbility(Guts))
      AbilityDamageModifier.attackerMultiplier(s, move) shouldEqual 1.3
  }

  test("Guts gives no multiplier without status") {
    val physMove = allMoves.collectFirst { case m: DamageMove if m.category == Physical => m }
    physMove.foreach: move =>
      val s = withSelfAbility(Guts)
      AbilityDamageModifier.attackerMultiplier(s, move) shouldEqual 1.0
  }

  test("RunAway restores speed to base when modified speed is lower than base") {
    val s = self(active(modifyStats(speed(multiply(0.5)))))(battle) // halves the speed
    val before = s.self.getActive.species.baseStats.speed.toInt
    run(RunAway, OnTurnStart)(s).self.getActive.modifiedStats.speed shouldEqual before
  }

  test("RunAway does not modify speed when modified speed is not lower than base") {
    val before = battle.self.getActive.modifiedStats.speed
    run(RunAway, OnTurnStart)(battle).self.getActive.modifiedStats.speed shouldEqual before
  }

  // WATER

  test("Torrent gives 1.5x multiplier to Water type moves when self HP <= 1/3") {
    val lowHpState = self(active(takeDamage(30)))(withSelfAbility(Torrent))
    waterMoveOpt.foreach: waterMove =>
      AbilityDamageModifier.attackerMultiplier(lowHpState, waterMove) shouldEqual 1.5
  }

  test("RainDish heals self by maxHp/16 when weather is Rain") {
    val s = withWeatherAndDamage(Rain)
    selfHp(run(RainDish, OnTurnEnd)(s)) shouldEqual selfHp(s) + maxSelfHp(s) / 16
  }

  test("RainDish does not heal when weather is not Rain") {
    run(RainDish, OnTurnEnd)(battle).self.getActive.currentHp shouldEqual battle.self.getActive.currentHp
  }

  test("WaterAbsorb heals self by maxHp/4 when hit by Water type move") {
    waterMoveOpt.foreach: waterMove =>
      val damaged = self(active(takeDamage(20)))(battle)
      val s = self(_.updateFlags(_.copy(lastOpponentMove = Some(waterMove))))(damaged)
      val result = run(WaterAbsorb, OnDamageTaken(Self))(s)
      result.self.getActive.currentHp shouldEqual s.self.getActive.currentHp + s.self.getActive.maxHp / 4
  }

  test("WaterAbsorb does not heal when hit by non-Water move") {
    fireMoveOpt.foreach: fireMove =>
      val s = self(_.updateFlags(_.copy(lastOpponentMove = Some(fireMove))))(battle)
      run(WaterAbsorb, OnDamageTaken(Self))(s).self.getActive.currentHp shouldEqual battle.self.getActive.currentHp
  }

  test("Hydration clears status condition when weather is Rain") {
    val s = setWeather(Rain)(self(active(addStatus(Burned)))(battle))
    s.self.getActive.statusCondition shouldBe defined
    run(Hydration, OnTurnStart)(s).self.getActive.statusCondition shouldBe empty
  }

  test("Hydration does not clear status condition when weather is not Rain") {
    val s = self(active(addStatus(Burned)))(battle)
    run(Hydration, OnTurnStart)(s).self.getActive.statusCondition shouldBe defined
  }

  test("Intimidate reduces opponent's active attack by 10%") {
    val before = battle.opponent.getActive.modifiedStats.attack
    val after = run(Intimidate, OnSwitchIn(Self))(battle).opponent.getActive.modifiedStats.attack
    after shouldEqual (before * 0.9).toInt
  }

  test("Moxie boosts self active attack by 10% after KO") {
    val before = battle.self.getActive.modifiedStats.attack
    val after = run(Moxie, OnKOTaken(Opponent))(battle).self.getActive.modifiedStats.attack
    after shouldEqual (before * 1.1).toInt
  }

  // GRASS

  test("Overgrow gives 1.5x multiplier to Grass type moves when self HP <= 1/3") {
    val lowHpState = self(active(takeDamage(30)))(withSelfAbility(Overgrow))
    grassMoveOpt.foreach: grassMove =>
      AbilityDamageModifier.attackerMultiplier(lowHpState, grassMove) shouldEqual 1.5
  }

  test("Chlorophyll doubles self speed when weather is HeavySunlight and not suppressed") {
    val s = withWeather(HeavySunlight)
    val before = s.self.getActive.modifiedStats.speed
    run(Chlorophyll, OnTurnStart)(s).self.getActive.modifiedStats.speed shouldEqual (before * 2)
  }

  test("Chlorophyll does not doubles self speed when weather is suppressed") {
    val s = self(_.updateFlags(_.copy(weatherSuppressed = true)))(withWeather(HeavySunlight))
    run(Chlorophyll, OnTurnStart)(s).self.getActive.modifiedStats.speed shouldEqual s.self.getActive.modifiedStats.speed
  }

  test("Chlorophyll does not doubles self speed when weather is not HeavySunlight") {
    val before = battle.self.getActive.modifiedStats.speed
    run(Chlorophyll, OnTurnStart)(battle).self.getActive.modifiedStats.speed shouldEqual before
  }

  test("ThickFat gives 0.5x multiplier to Fire type moves") {
    fireMoveOpt.foreach: fireMove =>
      val s = withOpponentAbility(ThickFat)
      AbilityDamageModifier.defenderMultiplier(s, fireMove) shouldEqual 0.5
  }

  test("Regenerator heals self by maxHp/3 when switching out") {
    val s = withDamagedSelf(30)
    selfHp(run(Regenerator, OnSwitchOut(Self))(s)) shouldEqual selfHp(s) + maxSelfHp(s) / 3
  }

  test("EffectSpore may apply a random status condition to opponent when hit (10 probabilistic trials)") {
    val results = (1 to 50).map(_ => run(EffectSpore, OnDamageTaken(Self))(battle))
    results.exists(_.opponent.getActive.statusCondition.isDefined) shouldBe true
    results.exists(_.opponent.getActive.statusCondition.isEmpty) shouldBe true
  }

  // ELECTRIC

  test("Static may paralyze opponent when hit (10 probabilistic trials)") {
    val results = (1 to 50).map(_ => run(Static, OnDamageTaken(Opponent))(battle))
    results.exists(_.opponent.getActive.statusCondition.contains(Paralyzed)) shouldBe true
    results.exists(_.opponent.getActive.statusCondition.isEmpty) shouldBe true
  }

  test("LightningRodLite heals self by maxHp/8 when last opponent move is Electric type") {
    electricMoveOpt.foreach: electricMove =>
      val damaged = self(active(takeDamage(20)))(battle)
      val s = self(_.updateFlags(_.copy(lastOpponentMove = Some(electricMove))))(damaged)
      selfHp(run(LightningRodLite, OnDamageTaken(Self))(s)) shouldEqual selfHp(s) + maxSelfHp(s) / 8
  }

  test("LightningRodLite does not heal when last opponent move is not Electric type") {
    fireMoveOpt.foreach: fireMove =>
      val s = self(_.updateFlags(_.copy(lastOpponentMove = Some(fireMove))))(battle)
      run(LightningRodLite, OnDamageTaken(Self))(s).self.getActive.currentHp shouldEqual s.self.getActive.currentHp
  }

  test("LightningRod boosts self special attack by 1.5x when last opponent move is Electric type") {
    electricMoveOpt.foreach: electricMove =>
      val s = self(_.updateFlags(_.copy(lastOpponentMove = Some(electricMove))))(battle)
      val before = s.self.getActive.modifiedStats.specialAttack
      run(LightningRod, OnDamageTaken(Self))(s).self.getActive.modifiedStats.specialAttack shouldEqual (before * 1.5).toInt
  }

  test("LightningRod does not boost self special attack when last opponent move is not Electric type") {
    fireMoveOpt.foreach: fireMove =>
      val s = self(_.updateFlags(_.copy(lastOpponentMove = Some(fireMove))))(battle)
      val before = s.self.getActive.modifiedStats.specialAttack
      run(LightningRod, OnDamageTaken(Self))(s).self.getActive.modifiedStats.specialAttack shouldEqual before
  }

  test("SurgeSurfer doubles self speed when Thunderstorm weather is active and not suppressed") {
    val s = withWeather(Thunderstorm)
    val before = s.self.getActive.modifiedStats.speed
    run(SurgeSurfer, OnTurnStart)(s).self.getActive.modifiedStats.speed shouldEqual (before * 2)
  }

  test("SurgeSurfer does not boost speed when weather is suppressed") {
    val s = self(_.updateFlags(_.copy(weatherSuppressed = true)))(withWeather(Thunderstorm))
    val before = s.self.getActive.modifiedStats.speed
    run(SurgeSurfer, OnTurnStart)(s).self.getActive.modifiedStats.speed shouldEqual before
  }

  test("SurgeSurfer does not boost speed when weather is not Thunderstorm"){
    val before = battle.self.getActive.modifiedStats.speed
    run(SurgeSurfer, OnTurnStart)(battle).self.getActive.modifiedStats.speed shouldEqual before
  }

  test("Aftermath damages opponent by maxHp/8 after KO") {
    enemyHp(run(Aftermath, OnKOTaken(Self))(battle)) shouldEqual enemyHp(battle) - maxEnemyHp(battle) / 8
  }

  test("VoltAbsorb heals self by maxHp/4 when hit by Electric type move") {
    electricMoveOpt.foreach: electricMove =>
      val damaged = self(active(takeDamage(20)))(battle)
      val s = self(_.updateFlags(_.copy(lastOpponentMove = Some(electricMove))))(damaged)
      selfHp(run(VoltAbsorb, OnDamageTaken(Self))(s)) shouldEqual selfHp(s) + maxSelfHp(s) / 4
  }

  test("VoltAbsorb does not heal when hit by non-Electric move") {
    fireMoveOpt.foreach: fireMove =>
      val s = self(_.updateFlags(_.copy(lastOpponentMove = Some(fireMove))))(battle)
      run(VoltAbsorb, OnDamageTaken(Self))(s).self.getActive.currentHp shouldEqual battle.self.getActive.currentHp
  }

  test("QuickFeet boosts self speed by 1.5x when self has a status condition") {
    val s = self(active(addStatus(Paralyzed)))(battle)
    val before = s.self.getActive.modifiedStats.speed
    run(QuickFeet, OnTurnStart)(s).self.getActive.modifiedStats.speed shouldEqual (before * 1.5).toInt
  }

  test("QuickFeet does not boost self speed when self has no status condition") {
    val before = battle.self.getActive.modifiedStats.speed
    run(QuickFeet, OnTurnStart)(battle).self.getActive.modifiedStats.speed shouldEqual before
  }

  // PSYCHIC

  test("Synchronize copies self status condition to opponent when self has a status condition") {
    val s = self(active(addStatus(Paralyzed)))(battle)
    run(Synchronize, OnDamageTaken(Self))(s).opponent.getActive.statusCondition shouldEqual Some(Paralyzed)
  }

  test("Synchronize does not copy status condition when self has no status condition") {
    run(Synchronize, OnDamageTaken(Self))(battle).opponent.getActive.statusCondition shouldEqual None
  }

  test("MagicGuard sets selfMagicGuardActive flag to true") {
    run(MagicGuard, OnTurnStart)(battle).self.flags.magicGuardActive shouldBe true
  }

  test("MagicGuard prevents self from taking indirect damage (e.g., weather, status)") {
    val s = withWeatherAndDamage(Rain, 20)
    val result = run(MagicGuard, OnTurnStart)(s)
    result.self.getActive.currentHp shouldEqual s.self.getActive.currentHp
  }

  test("Insomnia prevents self from falling asleep") {
    val s = withSelfAbility(Insomnia)
    val afterSleep = self(active(_.setStatus(Sleeping(3))))(s)
    afterSleep.self.getActive.statusCondition shouldBe empty
  }

  test("Insomnia does not block non-sleep status conditions") {
    val s = withSelfAbility(Insomnia)
    val afterBurn = self(active(_.setStatus(Burned)))(s)
    afterBurn.self.getActive.statusCondition shouldEqual Some(Burned)
  }

  // ONLY LOG ABILITY
  test("Forewarn does not modify battle state on switch in") {
    val result = run(Forewarn, OnSwitchIn(Self))(battle)
    result.copy(logs = battle.logs) shouldEqual battle
  }

  test("DrySkin heals self by maxHp/16 when weather is Rain") {
    val damaged = setWeather(Rain)(self(active(takeDamage(20)))(battle))
    selfHp(run(DrySkin, OnTurnEnd)(damaged)) shouldEqual selfHp(damaged) + maxSelfHp(damaged) / 16
  }

  test("DrySkin damages self by maxHp/16 when weather is HeavySunlight") {
    val s = withWeather(HeavySunlight)
    selfHp(run(DrySkin, OnTurnEnd)(s)) shouldEqual selfHp(s) - maxSelfHp(s) / 16
  }

  test("DrySkin does not modify HP in other weather conditions") {
    val s = withWeather(ClearSky)
    selfHp(run(DrySkin, OnTurnEnd)(s)) shouldEqual selfHp(s)
  }

  test("Pressure decreases last opponent's move PP by 1") {
    fireMoveOpt.foreach: fireMove =>
      val s = self(_.updateFlags(_.copy(lastOpponentMove = Some(fireMove))))(withOpponentMove(fireMove))
      val before = s.opponent.getActive.moves(fireMove.name).currentPp
      run(Pressure, OnDamageTaken(Self))(s).opponent.getActive.moves(fireMove.name).currentPp shouldEqual before - 1
  }

  test("CloudNine suppresses weather on switch in") {
    val s = withSelfAbility(CloudNine)
    run(CloudNine, OnSwitchIn(Self))(s).self.flags.weatherSuppressed shouldEqual true
    run(CloudNine, OnSwitchIn(Self))(s).opponent.flags.weatherSuppressed shouldEqual true
  }

  test("CloudNine on switch-in suppresses weather, so Chlorophyll does not boost speed") {
    val s = withWeather(HeavySunlight)
    val suppressed = run(CloudNine, OnSwitchIn(Self))(s)
    val before = suppressed.self.getActive.modifiedStats.speed
    run(Chlorophyll, OnTurnStart)(suppressed).self.getActive.modifiedStats.speed shouldEqual before
  }

  test("SwiftSwim double self speed when weather is Rain and not suppressed") {
    val s = withWeather(Rain)
    val before = s.self.getActive.modifiedStats.speed
    run(SwiftSwim, OnTurnStart)(s).self.getActive.modifiedStats.speed shouldEqual (before * 2)
  }

  test("SwiftSwim does not double self speed when weather is suppressed") {
    val s = self(_.updateFlags(_.copy(weatherSuppressed = true)))(withWeather(Rain))
    val before = s.self.getActive.modifiedStats.speed
    run(SwiftSwim, OnTurnStart)(s).self.getActive.modifiedStats.speed shouldEqual before
  }

  // POISON

  test("ShedSkin may clear self status condition (10 probabilistic trials)") {
    val s = self(active(addStatus(Poisoned)))(battle)
    val results = (1 to 50).map(_ => run(ShedSkin, OnTurnEnd)(s))
    results.exists(_.self.getActive.statusCondition.isEmpty) shouldBe true
    results.exists(_.self.getActive.statusCondition.isDefined) shouldBe true
  }

  test("PoisonTouch may poison opponent when hit (10 probabilistic trials)") {
    val results = (1 to 50).map(_ => run(PoisonTouch, OnDamageTaken(Opponent))(battle))
    results.exists(_.opponent.getActive.statusCondition.contains(Poisoned)) shouldBe true
    results.exists(_.opponent.getActive.statusCondition.isEmpty) shouldBe true
  }

  test("ShadowTag sets opponentSwitchBlocked flag to true on switch in") {
    val s = withSelfAbility(ShadowTag)
    run(ShadowTag, OnSwitchIn(Self))(s).opponent.flags.isSwitchBlocked shouldEqual true
  }

  test("LiquidOoze may damage opponent (10 probabilistic trials)") {
    val results = (1 to 50).map(_ => run(LiquidOoze, OnDamageTaken(Self))(battle))
    results.exists(_.opponent.getActive.currentHp < battle.opponent.getActive.currentHp) shouldBe true
    results.exists(_.opponent.getActive.currentHp == battle.opponent.getActive.currentHp) shouldBe true
  }

  test("CursedBody may set opponent move PP to 0 (10 probabilistic trials)") {
    fireMoveOpt.foreach: fireMove =>
      val s = self(_.updateFlags(_.copy(lastOpponentMove = Some(fireMove))))(withOpponentMove(fireMove))
      val results = (1 to 50).map(_ => run(CursedBody, OnDamageTaken(Self))(s))
      results.exists(_.opponent.getActive.moves(fireMove.name).currentPp == 0) shouldBe true
      results.exists(_.opponent.getActive.moves(fireMove.name).currentPp > 0) shouldBe true
  }

  test("Levitate gives 0.0x multiplier to Physical type moves") {
    physMoveOpt.foreach: physMove =>
      val s = withOpponentAbility(Levitate)
      AbilityDamageModifier.defenderMultiplier(s, physMove) shouldEqual 0.0
  }