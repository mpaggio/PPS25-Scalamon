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
import scalamon.logics.turns.Side.{Opponent, Self}

class TurnResolutionTest extends AnyFunSuite with StateFixtures:

  private val charmanderAlmostKO =  active(currentHp(decrease(38)))(player1)

  private val onlyCharmanderPlayer = playerInitialState(
    "Player1",
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
    getTurnResults(battle) shouldBe Ongoing
  }

  test("resolveTurn returns Ongoing when both sides have damaged but alive Pokemon"){
    val damaged = self(active(currentHp(decrease(10))))(opponent(active(currentHp(decrease(10))))(battle))
    getTurnResults(damaged) shouldBe Ongoing
  }
  
  test("resolveTurn SelfWins is triggered when all opponent's Pokemon are knocked out"){
    val opponentAllKO = opponent( all( currentHp( decrease( 999)))) (battle)
    getTurnResults(opponentAllKO) shouldBe Victory(opponentAllKO.self.name)
  }

  test("resolveTurn SelfWins takes priority over ForcedSwitch on a simultaneous KO"){
    val simultaneousKO = self(active(currentHp(decrease(1))))(opponent(all(currentHp(decrease(999))))(battleActiveLowHP))
    getTurnResults(simultaneousKO) shouldBe Victory(simultaneousKO.self.name)
  }

  test("resolveTurn SelfLoses is triggered when all self's Pokemon are knocked out"){
    val selfAllKO = self(active(currentHp(decrease(1))))(battleOnlyLowHP)
    getTurnResults(selfAllKO) shouldBe Victory(selfAllKO.opponent.name)
  }

  test("resolveTurn returns ForcedSwitch when self's active Pokemon is KO but bench is alive") {
    val activeKO = self (active( currentHp ( decrease (1))))(battleActiveLowHP)
    getTurnResults(activeKO) shouldBe a [ForcedSwitch]
  }

  test("resolveTurn ForcedSwitch candidates contain only alive bench Pokemon"){
    val activeKO = self ( active ( currentHp ( decrease(1))))(battleActiveLowHP)
    getTurnResults(activeKO) match
      case ForcedSwitch(switchRequests) =>
        assert(switchRequests.nonEmpty)
        assert(switchRequests.head.candidates.forall(ref => !isKnockedOut(player1.team(ref.value))))
      case _ => fail("Expected ForcedSwitch result")
  }


  test("resolveTurn returns OpponentForcedSwitch when opponent's active Pokemon is KO but bench is alive") {
    val opponentWithBench = playerInitialState(
      "Player2",
      Map("Squirtle" -> enemyPokemon, "Bulbasaur" -> enemyPokemon),
      "Squirtle"
    )
    val battleWithBench = battleState(player1, opponentWithBench)
    val opponentActiveKO = opponent( active ( currentHp( decrease(999))))(battleWithBench)
    getTurnResults(opponentActiveKO) shouldBe a [ForcedSwitch]
    getTurnResults(opponentActiveKO) match
      case ForcedSwitch(switchRequests) =>
        assert(switchRequests.nonEmpty)
        assert(switchRequests.head.side == Opponent)
  }

  test("resolveTurn returns BothForcedSwitch when both active Pokemon are KO but both sides have alive bench Pokemon") {
    val opponentWithBench = playerInitialState(
      "Player2",
      Map("Squirtle" -> enemyPokemon, "Bulbasaur" -> enemyPokemon),
      "Squirtle"
    )
    val battleWithBench = battleState(player1, opponentWithBench)
    val bothActiveKO = self(active(currentHp(decrease(999))))(opponent(active(currentHp(decrease(999))))(battleWithBench))
    getTurnResults(bothActiveKO) shouldBe a [ForcedSwitch]
    getTurnResults(bothActiveKO) match
      case ForcedSwitch(switchRequests) =>
        assert(switchRequests.size == 2)
        assert(switchRequests.exists(_.side == Self))
        assert(switchRequests.exists(_.side == Opponent))
      case _ => fail("Expected ForcedSwitch result")
  }

  test("endTurn does not mutate the original BattleState"){
    endTurn.foldLeft(battle)((state, transformer) => transformer(state))
    battle.self.activeId shouldBe "Charmander"
    battle.self.getActive.currentHp shouldBe 39
    battle.opponent.activeId shouldBe "Squirtle"
  }

  test("endTurn applies burn damage to the active burned Pokemon") {
    import scalamon.domain.alteredStatus.AlteredStatusModule.applyCondition
    import scalamon.domain.alteredStatus.AlteredStatus.Burned
    val burned = self(active(addStatus(Burned)))(battle)
    val hpBefore = burned.self.getActive.currentHp
    val afterBurn = Burned.applyCondition(burned)
    val expectedDamage = burned.self.getActive.maxHp / 8
    afterBurn.self.getActive.currentHp shouldBe (hpBefore - expectedDamage)
  }