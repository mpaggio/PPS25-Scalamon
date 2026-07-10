package scalamon.domain.pokemon.abilities

import scalamon.domain.moves.AlteredStatus.Burned
import scalamon.domain.moves.{DamageMove, DamageMoveCategory}
import scalamon.domain.moves.DamageMoveCategory.Physical
import scalamon.domain.pokemon.abilities.Ability.*
import scalamon.domain.types.Type.*
import scalamon.domain.weather.Weather.*
import scalamon.logics.state.BattleStateImpl.BattleState

/**
 * Multipliers of damage derived from the passive abilities.
 * Does not transform the battle state but contributes to the damage calculation
 * before the damage is applied to the state.
 */
object AbilityDamageModifier:

  final case class DamageModifierResult(multiplier: Double, logs: List[String])

  private def combine(results: List[DamageModifierResult]): DamageModifierResult =
    DamageModifierResult(
      multiplier = results.map(_.multiplier).product,
      logs = results.flatMap(_.logs)
    )

  private def allAbilities(slot: AbilitySlot):
    List[Ability] = List(Some(slot.primary), slot.secondary, slot.hidden).flatten

  private def noEffect: DamageModifierResult =
    DamageModifierResult(1.0, Nil)

  /**
   * Calculates the damage multiplier based on the defender's ability.
   *
   * @param state   the current battle state
   * @param move    the move being used by the attacking Pokémon
   * @param ability the ability of the defending Pokémon
   * @return the damage multiplier as a Double
   */
  private def abilityDefenseModifier(state: BattleState, move: DamageMove, ability: Ability): DamageModifierResult =
    ability match
      case ThickFat if move.moveType == Fire =>
        DamageModifierResult(
          0.5,
          List(s"[ThickFat] ${state.opponent.getActive.species.name} takes 50% less damage from Fire moves!")
        )
      case Levitate if move.category == Physical =>
        DamageModifierResult(
          0.0,
          List(s"[Levitate] ${state.opponent.getActive.species.name} is immune to Physical moves!")
        )
      case _ => noEffect

  /**
   * Calculates the damage multiplier based on the attacker's ability.
   * @param state the current battle state
   * @param move the move being used by the attacking Pokémon
   * @param ability the ability of the attacking Pokémon
   * @return the damage multiplier as a Double
   */
  private def abilityAttackModifier(state: BattleState, move: DamageMove, ability: Ability): DamageModifierResult = {
    val attacker = state.self.getActive
    val lowHp = attacker.currentHp <= attacker.maxHp / 3

    ability match
      case Blaze if lowHp && move.moveType == Fire =>
        DamageModifierResult(
          1.5,
          List(s"[Blaze] ${attacker.species.name} is using Blaze! Fire moves are boosted!")
        )
      case Torrent if lowHp && move.moveType == Water =>
        DamageModifierResult(
          1.5,
          List(s"[Torrent] ${attacker.species.name} is using Torrent! Water moves are boosted!")
        )
      case Overgrow if lowHp && move.moveType == Grass =>
        DamageModifierResult(
          1.5,
          List(s"[Overgrow] ${attacker.species.name} is using Overgrow! Grass moves are boosted!")
        )
      case SolarPower
        if state.weather == HeavySunlight
          && move.category == DamageMoveCategory.Special =>
        DamageModifierResult(
          1.3,
          List(s"[SolarPower] ${attacker.species.name}'s special moves are boosted in Heavy Sunlight!")
        )

      case Ability.FlashFire
        if state.flags.selfFlashFireActive && move.moveType == Fire =>
        DamageModifierResult(
          1.3,
          List(s"[FlashFire] ${attacker.species.name}'s Fire moves are boosted due to Flash Fire!")
        )
      case DroughtAura if move.moveType == Fire =>
        DamageModifierResult(
          1.1,
          List(s"[DroughtAura] ${attacker.species.name} boosts Fire moves by 10%!")
        )
      case Guts if attacker.statusCondition.isDefined
          && move.category == Physical =>
        val burnCompensation = if attacker.statusCondition.contains(Burned) then 2.0 else 1.0
        DamageModifierResult(
          1.3 * burnCompensation,
          List(s"[Guts] ${attacker.species.name} boosts its Physical Attack by 30% when it has a status condition!")
        )
      case _ => noEffect
  }

  def attackerModifier(state: BattleState, move: DamageMove): DamageModifierResult =
    val attacker = state.self.getActive
    val abilities = allAbilities(attacker.species.abilitySlot)
    combine(abilities.map(abilityAttackModifier(state, move, _)))

  def defenderModifier(state: BattleState, move: DamageMove): DamageModifierResult =
    val defender = state.opponent.getActive
    val abilities = allAbilities(defender.species.abilitySlot)
    combine(abilities.map(abilityDefenseModifier(state, move, _)))

  def attackerMultiplier(state: BattleState, move: DamageMove): Double =
    attackerModifier(state, move).multiplier

  def defenderMultiplier(state: BattleState, move: DamageMove): Double =
    defenderModifier(state, move).multiplier
    
  
    