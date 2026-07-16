package scalamon.logics.state

import org.scalatest.funsuite.AnyFunSuite
import scalamon.domain.moves.Accuracy.ProbabilityRoll
import scalamon.domain.moves.{CriticalMultiplier, DamageMove}
import scalamon.domain.moves.DamageMoveCategory.*
import scalamon.domain.moves.MoveDSL.move
import scalamon.database.MoveDatabase.{allMoves, ofType}
import scalamon.database.MyPokedex
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.pokemon.statistics.StatADT.fromInt
import scalamon.domain.pokemon.statistics.Stats
import scalamon.domain.types.Type.*
import scalamon.logics.state.BattleStateImpl.battleState
import scalamon.logics.damage.DamageMoveCalculatorImpl.getDamage
import scalamon.logics.state.PlayerStateModuleImpl.playerInitialState
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.StatsStateModuleImpl.*
import scalamon.logics.damage.DamagePolicy.Medium.given
import scalamon.logics.state.MoveStateModuleImpl.*
import scalamon.logics.weather.WeatherSystem

class DamageMoveCalculatorTest extends AnyFunSuite:
  val baseStats = Stats(
    hp = fromInt(100),
    attack = fromInt(50),
    defense = fromInt(30),
    specialAttack = fromInt(40),
    specialDefense = fromInt(20),
    speed = fromInt(60)
  )

  val attackerSpecies: Pokemon = MyPokedex.allPokemons.find(_.name == "Charmander").get
  val defenderSpecies: Pokemon = MyPokedex.allPokemons.find(_.name == "Bulbasaur").get

  val attackerMoves: Map[String, MoveState] =
    allMoves.ofType(attackerSpecies.pokemonType).take(4).map(m => m.name -> moveInitialState(m)).toMap
  val defenderMoves: Map[String, MoveState] =
    allMoves.ofType(defenderSpecies.pokemonType).take(4).map(m => m.name -> moveInitialState(m)).toMap

  val attackerPokemonState: PokemonStateModuleImpl.Pks = pokemonInitialState(attackerSpecies, attackerMoves)
  val defenderPokemonState: PokemonStateModuleImpl.Pks = pokemonInitialState(defenderSpecies, defenderMoves)

  val playerAtk: PlayerStateModuleImpl.Ps = playerInitialState(
    "Player1",
    team = Map("Charmander" -> attackerPokemonState),
    active = "Charmander"
  )
  val playerDef: PlayerStateModuleImpl.Ps = playerInitialState(
    "Player2",
    team = Map("Bulbasaur" -> defenderPokemonState),
    active = "Bulbasaur"
  )

  val state: BattleStateImpl.Bs = battleState(playerAtk, playerDef)

  val physicalMove: DamageMove = move named "Tackle" withPower 40 withPP 35 withAccuracy 100 withType Normal as Physical
  val specialMove: DamageMove = move named "Flamethrower" withPower 90 withPP 15 withAccuracy 100 withType Fire as Special
  val stabMove: DamageMove = move named "Ember" withPower 40 withPP 25 withAccuracy 100 withType Fire as Special
  val superMove: DamageMove = move named "Surf" withPower 90 withPP 15 withAccuracy 100 withType Water as Special
  val notVeryEffectiveMove: DamageMove = move named "Vine Whip" withPower 45 withPP 25 withAccuracy 100 withType Grass as Special
  val noEffectMove: DamageMove = move named "Thunder Wave" withPower 1 withPP 20 withAccuracy 90 withType Electric as Special

  test("getDamage should return a positive value with valid inputs"){
    given ProbabilityRoll = () => 100 // Force no critical hit
    val damage = getDamage(state, physicalMove).damage
    assert(damage > 0, s"Expected damage to be positive, but got $damage")
  }

  test("getDamage should increase if attacker's attack stat increases (Physical)"){
    given ProbabilityRoll = () => 100 // Force no critical hit
    val boostedPlayerAtk = playerInitialState(
      "Player1",
      team = Map("Charmander" -> modifyStats(attack(increase(50)))(attackerPokemonState)),
      active = "Charmander"
    )
    val stateNormal = battleState(playerAtk, playerDef)
    val stateBoosted = battleState(boostedPlayerAtk, playerDef)

    val damageNormal = getDamage(stateNormal, physicalMove).damage
    val damageBoosted = getDamage(stateBoosted, physicalMove).damage

    assert(damageBoosted > damageNormal,
      s"Expected boosted damage ($damageBoosted) > normal damage ($damageNormal)")
  }

  test("getDamage should decrease if defender's defense stat increases (Physical)") {
    given ProbabilityRoll = () => 100 // Force no critical hit
    val tankPlayerDef = playerInitialState(
      "Player1",
      team = Map("Bulbasaur" -> modifyStats(defense(increase(50)))(defenderPokemonState)),
      active = "Bulbasaur"
    )
    val stateNormal = battleState(playerAtk, playerDef)
    val stateTank = battleState(playerAtk, tankPlayerDef)

    val damageNormal = getDamage(stateNormal, physicalMove).damage
    val damageTank = getDamage(stateTank, physicalMove).damage

    assert(damageTank < damageNormal,
      s"Expected boosted damage ($damageTank) < normal damage ($damageNormal)")
  }

  test("getDamage should increase if attacker's specialAttack stat increases (Special)") {
    given ProbabilityRoll = () => 100 // Force no critical hit
    val boostedPlayerAtk = playerInitialState(
      "Player1",
      team = Map("Charmander" -> modifyStats(specialAttack(increase(50)))(attackerPokemonState)),
      active = "Charmander"
    )
    val stateNormal = battleState(playerAtk, playerDef)
    val stateBoosted = battleState(boostedPlayerAtk, playerDef)

    val damageNormal = getDamage(stateNormal, specialMove).damage
    val damageBoosted = getDamage(stateBoosted, specialMove).damage

    assert(damageBoosted > damageNormal,
      s"Expected boosted damage ($damageBoosted) > normal damage ($damageNormal)")
  }

  test("getDamage should decrease if defender's specialDefense stat increases (Special)") {
    given ProbabilityRoll = () => 100 // Force no critical hit
    val tankPlayerDef = playerInitialState(
      "Player2",
      team = Map("Bulbasaur" -> modifyStats(specialDefense(increase(50)))(defenderPokemonState)),
      active = "Bulbasaur"
    )
    val stateNormal = battleState(playerAtk, playerDef)
    val stateTank = battleState(playerAtk, tankPlayerDef)

    val damageNormal = getDamage(stateNormal, specialMove).damage
    val damageTank = getDamage(stateTank, specialMove).damage

    assert(damageTank < damageNormal,
      s"Expected tank damage ($damageTank) > normal damage ($damageNormal)")
  }

  test("getDamage should use different stats for Physical and Special move categories") {
    given ProbabilityRoll = () => 100 // Force no critical hit
    val damagePhysical = getDamage(state, physicalMove).damage
    val damageSpecial = getDamage(state, specialMove).damage

    assert(damagePhysical != damageSpecial,
      "Expected different damage values for Physical and Special moves, but got the same value")
  }

  test("getDamage should produce higher damage for higher move power") {
    given ProbabilityRoll = () => 100 // Force no critical hit
    val lowPowerMove = move named "Low" withPower 20 withPP 35 withAccuracy 100 withType Normal as Physical
    val highPowerMove = move named "High" withPower 80 withPP 35 withAccuracy 100 withType Normal as Physical
    val damageLow = getDamage(state, lowPowerMove).damage
    val damageHigh = getDamage(state, highPowerMove).damage
    assert(damageHigh > damageLow,
      s"Expected higher power move to deal more damage, but got $damageHigh <= $damageLow")
  }

  test("getDamage with STAB should be greater than without STAB, all else equal") {
    given ProbabilityRoll = () => 100 // Force no critical hit
    val noSTABMove = move named "Tackle" withPower 40 withPP 35 withAccuracy 100 withType Normal as Special
    val stabMove = move named "Ember" withPower 40 withPP 25 withAccuracy 100 withType Fire as Special
    val damageNoSTAB = getDamage(state, noSTABMove).damage
    val damageSTAB = getDamage(state, stabMove).damage
    assert(damageSTAB > damageNoSTAB,
      s"Expected STAB ($damageSTAB) to be greater than no STAB ($damageNoSTAB), but got $damageSTAB <= $damageNoSTAB")
  }

  test("getDamage should apply X2 multiplier when move is SuperEffective"){
    given ProbabilityRoll = () => 100 // Force no critical hit
    val damageNeutral = getDamage(state, physicalMove).damage
    val damageSuperEffective = getDamage(state, specialMove).damage
    assert(damageSuperEffective > damageNeutral,
      s"Expected super effective damage ($damageSuperEffective) to be greater than neutral damage ($damageNeutral)," +
        s" but got $damageSuperEffective <= $damageNeutral")
  }

  test("getDamage should apply 0.5 multiplier when move is NotVeryEffective"){
    given ProbabilityRoll = () => 100 // Force no critical hit
    val damageNeutral = getDamage(state, physicalMove).damage
    val damageNotVeryEffective = getDamage(state, notVeryEffectiveMove).damage
    assert(damageNotVeryEffective < damageNeutral,
      s"Expected not very effective damage ($damageNotVeryEffective) to be less than neutral damage ($damageNeutral)," +
        s" but got $damageNotVeryEffective >= $damageNeutral")
  }

  test("getDamage with STAB & SuperEffective should produce higher damage than without STAB and Neutral effectiveness") {
    given ProbabilityRoll = () => 100 // Force no critical hit
    val NoSTABAndNeutralDamage = getDamage(state, physicalMove).damage
    val STABAndSuperEffectiveDamage = getDamage(state, stabMove).damage

    assert(STABAndSuperEffectiveDamage > NoSTABAndNeutralDamage,
      s"Expected STAB + Super Effective damage ($STABAndSuperEffectiveDamage) to be greater than no STAB + Neutral damage ($NoSTABAndNeutralDamage), " +
        s"but got $STABAndSuperEffectiveDamage <= $NoSTABAndNeutralDamage")
  }

  test("getDamage should be lower in Easy Mode than in Medium Mode") {
    given ProbabilityRoll = () => 100 // Force no critical hit
    import scalamon.logics.damage.DamagePolicy.Easy.given
    val easyDamage = getDamage(state, physicalMove).damage
    {
      import scalamon.logics.damage.DamagePolicy.Medium.given
      val mediumDamage = getDamage(state, physicalMove).damage
      assert(easyDamage < mediumDamage,
      s"Expected damage in Easy Mode ($easyDamage) to be less than damage in Medium Mode ($mediumDamage)," +
        s" but got $easyDamage >= $mediumDamage")
    }
  }

  test("getDamage should be lower in Medium Mode than in Hard Mode") {
    given ProbabilityRoll = () => 100 // Force no critical hit
    import scalamon.logics.damage.DamagePolicy.Medium.given
    val mediumDamage = getDamage(state, physicalMove).damage
    {
      import scalamon.logics.damage.DamagePolicy.Hard.given
      val hardDamage = getDamage(state, physicalMove).damage
      assert(mediumDamage < hardDamage,
        s"Expected damage in Medium Mode ($mediumDamage) to be less than damage in Hard Mode ($hardDamage)," +
          s" but got $mediumDamage >= $hardDamage")
    }
  }

  test("getDamage with critical hit should deal 1.5x more damage") {
    val damageCritical = locally: // To separate the scope of the given instance
      given ProbabilityRoll = () => 1 // Force a critical hit
      getDamage(state, physicalMove).damage
    val damageNormal = locally:
      given ProbabilityRoll = () => 100 // Force no critical hit
      getDamage(state, physicalMove).damage
    assert(damageCritical == (damageNormal * 1.5).toInt || damageCritical == (damageNormal * 1.5).toInt + 1,
      s"Expected critical ($damageCritical) to be circa = normal * 1.5 (${(damageNormal * 1.5).toInt})")
  }

  test("getDamage with CriticalMultiplier effect should have higher critical chance") {
    val highCriticalMove = DamageMove(
      name = "HighCrit",
      power = physicalMove.power,
      pp = physicalMove.pp,
      accuracy = physicalMove.accuracy,
      moveType = Normal,
      category = Physical,
      effect = Some(CriticalMultiplier(2)) // Double the critical hit chance
    )
    val damageBoosted = locally:
      given ProbabilityRoll = () => 10 // Force a critical hit with boosted chance
      getDamage(state, highCriticalMove).damage
    val damageNormal = locally:
      given ProbabilityRoll = () => 10 // Force a critical hit with normal chance
      getDamage(state, physicalMove).damage
    assert(damageBoosted > damageNormal,
      s"Expected high critical move ($damageBoosted) > normal move ($damageNormal) at roll = 10")
  }