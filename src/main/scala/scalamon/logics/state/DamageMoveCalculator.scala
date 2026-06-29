package scalamon.logics.state

import scalamon.domain.moves.Accuracy.{ProbabilityRoll, accuracyFromPercent}
import scalamon.domain.moves.{CriticalMultiplier, DamageMove}
import scalamon.domain.pokemon.abilities.AbilityDamageModifier
import scalamon.logics.weather.WeatherSystem

/**
 * This trait defines the interface for calculating the move's damage on the battle.
 */
trait DamageMoveCalculator:
  type BattleState
  type StatsState
  type Damage
  type Move

  def getDamage(state: BattleState, move: Move)(using DamagePolicy, WeatherSystem, ProbabilityRoll): Damage

object DamageMoveCalculatorImpl extends DamageMoveCalculator:

  import scalamon.domain.moves.DamageMoveCategory.*
  import scalamon.domain.types.*

  override type BattleState = BattleStateImpl.BattleState
  override type StatsState = StatsStateModuleImpl.StatsState
  override type Damage = Int
  override type Move = scalamon.domain.moves.DamageMove

  /**
   * Calculates the move's damage following the standard Pokémon damage formula.
   * @param state the current battle state
   * @param move the move being used by the attacking Pokémon
   * @param policy the difficulty level of the battle
   * @return the calculated damage as an integer
   */
  def getDamage(state: BattleState, move: Move)(using policy: DamagePolicy, weather: WeatherSystem, probabilityRoll: ProbabilityRoll): Damage =
    val attacker = state.self.getActive
    val defender = state.opponent.getActive

    // (((((2 * 50) / 5) + 2) * Power * (A / D)) / 50 + 2).floor * STAB * TypeEffectiveness * WhetherMultiplier

    val power = move.power.asInt

    val baseCritChance = 6.25

    val criticalChanceMultiplier = move.effect match
      case Some(CriticalMultiplier(m)) => m
      case _ => 1

    val isCritical = accuracyFromPercent((baseCritChance * criticalChanceMultiplier).toInt).test

    val criticalDamageMultiplier = if isCritical then 1.5 else 1.0

    val (atk, def_) = move.category match
      case Physical =>
        val a = if isCritical then attacker.modifiedStats.attack.max(attacker.species.baseStats.attack.toInt)
        else attacker.modifiedStats.attack
        val d = if isCritical then defender.modifiedStats.defense.min(defender.species.baseStats.defense.toInt)
        else defender.modifiedStats.defense
        (a, d)
      case Special =>
        val a = if isCritical then attacker.modifiedStats.specialAttack.max(attacker.species.baseStats.specialAttack.toInt)
        else attacker.modifiedStats.specialAttack
        val d = if isCritical then defender.modifiedStats.specialDefense.min(defender.species.baseStats.specialDefense.toInt)
        else defender.modifiedStats.specialDefense
        (a, d)

    val baseFormula = (((2.0 * 50 / 5 + 2) * power * atk.toDouble / def_.toDouble) / 50 + 2).toInt

    val stab = if attacker.species.pokemonType == move.moveType then 1.5 else 1.0

    val typeEffectiveness = move.moveType.multiplierAgainst(defender.species.pokemonType)

    val abilityAtkMulti = AbilityDamageModifier.attackerMultiplier(state, move)
    val abilityDefMulti = AbilityDamageModifier.defenderMultiplier(state, move)

    val weatherMulti =
      if state.flags.weatherSuppressed then 1.0
      else weather.movePowerMultiplier(state.weather, move.moveType)

    (baseFormula * stab * typeEffectiveness * policy.multiplier * criticalDamageMultiplier * abilityAtkMulti * abilityDefMulti * weatherMulti).toInt