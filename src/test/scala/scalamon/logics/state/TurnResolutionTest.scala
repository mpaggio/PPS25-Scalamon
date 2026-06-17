package scalamon.logics.state

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.shouldBe
import scalamon.logics.state.StatsStateModuleImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.BattleStateImpl.*
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

  /* STILL CANT TRY THIS, THE DYNAMIC HP IS NOT IMPLEMENTED YET
  test("isKnockedOut returns true for a Pokémon with 0 HP") {
    val knockedOut = myPokemon currentHp (_ decrease 39)
    isKnockedOut(knockedOut) shouldBe true
  }
  */

  test("isDefeated returns false when the team has all Pokemon alive") {
    isDefeated(player1) shouldBe false
  }

  test("isDefeated returns false when at least one Pokemon is alive") {
    isDefeated(charmanderAlmostKO) shouldBe false
  }

  test("isDefeated returns false for a single-pokemon team with the Pokemon alive"){
    isDefeated(onlyCharmanderPlayer) shouldBe false
  }

  test("needsForcedSwitch returns false when the active Pokemon is alive"){
    needsForcedSwitch(player1) shouldBe false
  }

  test("needsForcedSwitch returns false when the active pokemon is at 1 HP"){
    needsForcedSwitch(charmanderAlmostKO) shouldBe false
  }

  test("needsForcedSwitch returns false when all team members are at 1 HP"){
    val allLow = player1 all (_ currentHp (_ decrease 38))
    needsForcedSwitch(allLow) shouldBe false
  }

  test("resolveTurn returns Ongoing when no Pokemon is knocked out") {
    resolveTurn(battle) shouldBe Ongoing(battle)
  }

  test("resolveTurn returns Ongoing when both sides have damaged but alive pokemon"){
    val damaged = battle
      .self(_ active (_ currentHp (_ decrease 10)))
      .opponent(_ active (_ currentHp (_ decrease 10)))
    resolveTurn(damaged) shouldBe Ongoing(damaged)
  }