package scalamon.logics.turns

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.{a, be, shouldBe, shouldEqual, shouldNot}
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.StateFixtures
import scalamon.logics.state.StatsStateModuleImpl.*
import scalamon.logics.turns.PokemonRef
import scalamon.logics.turns.TurnResolutionImpl.*
import scalamon.logics.turns.TurnResult.*
import scalamon.domain.moves.Accuracy.given

class TurnResolutionTest extends AnyFunSuite with StateFixtures:

  private val charmanderAlmostKO =  active(currentHp(decrease(38)))(player1)

  private val onlyCharmanderPlayer = playerInitialState(
    Map("Charmander" -> myPokemon),
    "Charmander"
  )

  private val onlyCharmanderPlayerAlmostKO = active(currentHp(decrease(38)))(onlyCharmanderPlayer)

  private val battleActiveLowHP = battleState(charmanderAlmostKO, player2)

  private val battleOnlyLowHP = battleState(onlyCharmanderPlayerAlmostKO, player2)

  test("isKnockedOut returns false for a Pokemon with full HP") {
    isKnockedOut(myPokemon) shouldBe false
  }

  test("isKnockedOut returns false for a Pokemon with 1 HP remaining") {
    val almostDead = currentHp(decrease(38))(myPokemon)  /* Charmander has 39 HP */
    isKnockedOut(almostDead) shouldBe false
  }

  test("isKnockedOut returns true for a Pokémon with 0 HP") {
    val knockedOut = currentHp(decrease(39))(myPokemon)
    isKnockedOut(knockedOut) shouldBe true
  }

  test("isKnockedOut returns true for a Pokémon with negative HP") {
    val killed = currentHp(decrease(999))(myPokemon)
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
    val soloKO = active(currentHp(decrease(999)))(onlyCharmanderPlayer)
    isDefeated(soloKO) shouldBe true
  }

  test("isDefeated returns true when all team Pokemon are knocked out") {
    val allKO = all(currentHp(decrease(999)))(player1)
    isDefeated(allKO) shouldBe true
  }

  test("needsForcedSwitch returns false when the active Pokemon is alive"){
    needsForcedSwitch(player1) shouldBe false
  }

  test("needsForcedSwitch returns false when the active Pokemon is at 1 HP"){
    needsForcedSwitch(charmanderAlmostKO) shouldBe false
  }

  test("needsForcedSwitch returns false when all team members are at 1 HP"){
    val allLow =  all(currentHp(decrease(38)))(player1)
    needsForcedSwitch(allLow) shouldBe false
  }

  test("needsForcedSwitch returns true when active is KO but bench is alive"){
    val activeKO = active(currentHp(decrease(999)))(player1)
    needsForcedSwitch(activeKO) shouldBe true
  }

  test("needsForcedSwitch returns false when active is KO and all the bench is also KO"){
    val allKO = all(currentHp(decrease(999)))(player1)
    needsForcedSwitch(allKO) shouldBe false
  }

  test("resolveTurn returns Ongoing when no Pokemon is knocked out") {
    resolveTurn(battle) shouldBe Ongoing(battle)
  }

  test("resolveTurn returns Ongoing when both sides have damaged but alive Pokemon"){
    val damaged = self(active(currentHp(decrease(10))))(opponent(active(currentHp(decrease(10))))(battle))
    resolveTurn(damaged) shouldBe Ongoing(damaged)
  }
  
  test("resolveTurn SelfWins is triggered when all opponent's Pokemon are knocked out"){
    val opponentAllKO = opponent( all( currentHp( decrease( 999)))) (battle)
    resolveTurn(opponentAllKO) shouldBe SelfWins(opponentAllKO)
  }

  test("resolveTurn SelfWins takes priority over ForcedSwitch on a simultaneous KO"){
    val simultaneousKO = self(active(currentHp(decrease(1))))(opponent(all(currentHp(decrease(999))))(battleActiveLowHP))
    resolveTurn(simultaneousKO) shouldBe SelfWins(simultaneousKO)
  }

  test("resolveTurn SelfLoses is triggered when all self's Pokemon are knocked out"){
    val selfAllKO = self(active(currentHp(decrease(1))))(battleOnlyLowHP)
    resolveTurn(selfAllKO) shouldBe SelfLoses(selfAllKO)
  }

  test("resolveTurn returns ForcedSwitch when self's active Pokemon is KO but bench is alive") {
    val activeKO = self (active( currentHp ( decrease (1))))(battleActiveLowHP)
    resolveTurn(activeKO) shouldBe a [ForcedSwitch]
  }

  test("resolveTurn ForcedSwitch candidates contain only alive bench Pokemon"){
    val activeKO = self ( active ( currentHp ( decrease(1))))(battleActiveLowHP)
    resolveTurn(activeKO) match
      case ForcedSwitch(_, candidates) =>
        assert(candidates.nonEmpty)
        assert(candidates.forall(ref => !isKnockedOut(player1.team(ref.value))))
      case _ => fail("Expected ForcedSwitch result")
  }


  test("resolveTurn returns OpponentForcedSwitch when opponent's active Pokemon is KO but bench is alive") {
    val opponentWithBench = playerInitialState(
      Map("Squirtle" -> enemyPokemon, "Bulbasaur" -> enemyPokemon),
      "Squirtle"
    )
    val battleWithBench = battleState(player1, opponentWithBench)
    val opponentActiveKO = opponent( active ( currentHp( decrease(999))))(battleWithBench)
    resolveTurn(opponentActiveKO) shouldBe a [OpponentForcedSwitch]
  }

  test("resolveTurn returns BothForcedSwitch when both active Pokemon are KO but both sides have alive bench Pokemon") {
    val opponentWithBench = playerInitialState(
      Map("Squirtle" -> enemyPokemon, "Bulbasaur" -> enemyPokemon),
      "Squirtle"
    )
    val battleWithBench = battleState(player1, opponentWithBench)
    val bothActiveKO = self(active(currentHp(decrease(999))))(opponent(active(currentHp(decrease(999))))(battleWithBench))
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
    endTurn(battle)
    battle.self.activeId shouldBe "Charmander"
    battle.self.getActive.currentHp shouldBe 39
    battle.opponent.activeId shouldBe "Squirtle"
  }

  test("endTurn applies burn damage to the active burned Pokemon") {
    import scalamon.logics.state.AlteredStatusModule.applyCondition
    import scalamon.domain.moves.AlteredStatus.Burned
    val burned = self(active(_.setStatus(Burned)))(battle)
    val hpBefore = burned.self.getActive.currentHp
    val afterBurn = Burned.applyCondition(burned)
    val expectedDamage = burned.self.getActive.maxHp / 8
    afterBurn.self.getActive.currentHp shouldBe (hpBefore - expectedDamage)
  }