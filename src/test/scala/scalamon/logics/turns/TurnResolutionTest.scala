package scalamon.logics.turns

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.{a, be, shouldBe, shouldEqual, shouldNot}
import scalamon.domain.weather.Weather.ClearSky
import scalamon.logics.battle.WeatherState
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.StateFixtures
import scalamon.logics.state.StatsStateModuleImpl.*
import scalamon.logics.turns.PokemonRef
import scalamon.logics.turns.TurnResolutionImpl.*
import scalamon.logics.turns.TurnResult.*

class TurnResolutionTest extends AnyFunSuite with StateFixtures:

  private val charmanderAlmostKO = player1 active (_ currentHp (_ decrease 38))

  private val onlyCharmanderPlayer = playerState(
    Map("Charmander" -> myPokemon),
    "Charmander"
  )

  private val onlyCharmanderPlayerAlmostKO = onlyCharmanderPlayer active (_ currentHp (_ decrease 38))

  private val battleActiveLowHP = battleState(charmanderAlmostKO, player2)

  private val battleOnlyLowHP = battleState(onlyCharmanderPlayerAlmostKO, player2)

  test("isKnockedOut returns false for a Pokemon with full HP") {
    isKnockedOut(myPokemon) shouldBe false
  }

  test("isKnockedOut returns false for a Pokemon with 1 HP remaining") {
    val almostDead = myPokemon currentHp (_ decrease 38)  /* Charmander has 39 HP */
    isKnockedOut(almostDead) shouldBe false
  }

  test("isKnockedOut returns true for a Pokémon with 0 HP") {
    val knockedOut = myPokemon currentHp (_ decrease 39)
    isKnockedOut(knockedOut) shouldBe true
  }

  test("isKnockedOut returns true for a Pokémon with negative HP") {
    val killed = myPokemon currentHp (_ decrease 999)
    isKnockedOut(killed) shouldBe true
  }

  test("isDefeated returns false when the team has all Pokemon alive") {
    isDefeated(player1) shouldBe false
  }

  test("isDefeated returns false when at least one Pokemon is alive") {
    isDefeated(charmanderAlmostKO) shouldBe false
  }

  test("isDefeated returns false for a single-pokemon team with the Pokemon alive"){
    isDefeated(onlyCharmanderPlayer) shouldBe false
  }

  test("isDefeated returns true for a single-pokemon team when the only Pokemon is knocked out") {
    val soloKO = onlyCharmanderPlayer active (_ currentHp (_ decrease 999))
    isDefeated(soloKO) shouldBe true
  }

  test("isDefeated returns true when all team Pokemon are knocked out") {
    val allKO = player1 all (_ currentHp (_ decrease 999))
    isDefeated(allKO) shouldBe true
  }

  test("needsForcedSwitch returns false when the active Pokemon is alive"){
    needsForcedSwitch(player1) shouldBe false
  }

  test("needsForcedSwitch returns false when the active Pokemon is at 1 HP"){
    needsForcedSwitch(charmanderAlmostKO) shouldBe false
  }

  test("needsForcedSwitch returns false when all team members are at 1 HP"){
    val allLow = player1 all (_ currentHp (_ decrease 38))
    needsForcedSwitch(allLow) shouldBe false
  }

  test("needsForcedSwitch returns true when active is KO but bench is alive"){
    val activeKO = player1 active (_ currentHp (_ decrease 999))
    needsForcedSwitch(activeKO) shouldBe true
  }

  test("needsForcedSwitch returns false when active is KO and all the bench is also KO"){
    val allKO = player1 all (_ currentHp (_ decrease 999))
    needsForcedSwitch(allKO) shouldBe false
  }

  test("resolveTurn returns Ongoing when no Pokemon is knocked out") {
    resolveTurn(battle) shouldBe Ongoing(battle)
  }

  test("resolveTurn returns Ongoing when both sides have damaged but alive Pokemon"){
    val damaged = battle
      .self(_ active (_ currentHp (_ decrease 10)))
      .opponent(_ active (_ currentHp (_ decrease 10)))
    resolveTurn(damaged) shouldBe Ongoing(damaged)
  }

  test("resolveTurn SelfWins is triggered when all opponent's Pokemon are knocked out"){
    val opponentAllKO = battle opponent(_ all (_ currentHp (_ decrease 999)))
    resolveTurn(opponentAllKO) shouldBe SelfWins(opponentAllKO)
  }

  test("resolveTurn SelfWins takes priority over ForcedSwitch on a simultaneous KO"){
    val simultaneousKO = battleActiveLowHP
      .self(_ active (_ currentHp (_ decrease 1)))
      .opponent(_ all (_ currentHp (_ decrease 999)))
    resolveTurn(simultaneousKO) shouldBe SelfWins(simultaneousKO)
  }

  test("resolveTurn SelfLoses is triggered when all self's Pokemon are knocked out"){
    val selfAllKO = battleOnlyLowHP self(_ active (_ currentHp (_ decrease 1)))
    resolveTurn(selfAllKO) shouldBe SelfLoses(selfAllKO)
  }

  test("resolveTurn returns ForcedSwitch when self's active Pokemon is KO but bench is alive") {
    val activeKO = battleActiveLowHP self (_ active (_ currentHp (_ decrease 1)))
    resolveTurn(activeKO) shouldBe a [ForcedSwitch]
  }

  test("resolveTurn ForcedSwitch candidates contain only alive bench Pokemon"){
    val activeKO = battleActiveLowHP self (_ active (_ currentHp (_ decrease 1)))
    resolveTurn(activeKO) match
      case ForcedSwitch(_, candidates) =>
        assert(candidates.nonEmpty)
        assert(candidates.forall(ref => !isKnockedOut(player1.team(ref.value))))
      case _ => fail("Expected ForcedSwitch result")
  }


  test("resolveTurn returns OpponentForcedSwitch when opponent's active Pokemon is KO but bench is alive") {
    val opponentWithBench = playerState(
      Map("Squirtle" -> enemyPokemon, "Bulbasaur" -> enemyPokemon),
      "Squirtle"
    )
    val battleWithBench = battleState(player1, opponentWithBench)
    val opponentActiveKO = battleWithBench opponent (_ active (_ currentHp (_ decrease 999)))
    resolveTurn(opponentActiveKO) shouldBe a [OpponentForcedSwitch]
  }

  test("resolveTurn returns BothForcedSwitch when both active Pokemon are KO but both sides have alive bench Pokemon") {
    val opponentWithBench = playerState(
      Map("Squirtle" -> enemyPokemon, "Bulbasaur" -> enemyPokemon),
      "Squirtle"
    )
    val battleWithBench = battleState(player1, opponentWithBench)
    val bothActiveKO = battleWithBench
      .self(_ active (_ currentHp (_ decrease 999)))
      .opponent(_ active (_ currentHp (_ decrease 999)))
    resolveTurn(bothActiveKO) shouldBe a [BothForcedSwitch]
  }

  test("applyForcedSwitch changes the active Pokemon with the requested one"){
    val switched = applyForcedSwitch(player1, PokemonRef("Bulbasaur"))
    switched.activeId shouldBe "Bulbasaur"
  }

  test("applyForcedSwitch does not alter the team HP values"){
    val switched = applyForcedSwitch(player1, PokemonRef("Bulbasaur"))
    switched.team("Charmander").currentHp shouldEqual
      player1.team("Charmander").currentHp
    switched.team("Bulbasaur").currentHp shouldEqual
      player1.team("Bulbasaur").currentHp
  }

  test("applyForcedSwitch returns unchanged PlayerState for unknown PokemonRef"){
    val unchanged = applyForcedSwitch(player1, PokemonRef("MewTwo"))
    unchanged.activeId shouldBe player1.activeId
    unchanged.activeId shouldNot be ("MewTwo")
  }

  test("applyForcedSwitch does not modify original PlayerState"){
    applyForcedSwitch(player1, PokemonRef("Bulbasaur"))
    player1.activeId shouldBe "Charmander"
  }

  test("applyForcedSwitch result has the new pokemon as the active one"){
    val switched = applyForcedSwitch(player1, PokemonRef("Bulbasaur"))
    switched.getActive shouldEqual player1.team("Bulbasaur")
  }

  test("endTurn does not mutate the original BattleState"){
    endTurn(battle, WeatherState(ClearSky))
    battle.self.activeId shouldBe "Charmander"
    battle.self.getActive.currentHp shouldBe 39
    battle.opponent.activeId shouldBe "Squirtle"
  }

  test("endTurn applies burn damage to the active burned Pokemon") {
    import scalamon.logics.state.AlteredStatusModule.applyCondition
    import scalamon.domain.moves.AlteredStatus.Burned
    val burned = battle self (_ active (_ setStatus scalamon.domain.moves.AlteredStatus.Burned))
    val hpBefore = burned.self.getActive.currentHp
    val afterBurn = Burned.applyCondition(burned)
    val expectedDamage = burned.self.getActive.maxHp / 8
    afterBurn.self.getActive.currentHp shouldBe (hpBefore - expectedDamage)
  }