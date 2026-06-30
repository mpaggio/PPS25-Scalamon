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
  
  private def allAbilities(slot: AbilitySlot):
    List[Ability] = List(Some(slot.primary), slot.secondary, slot.hidden).flatten

  /**
   * Calculates the damage multiplier based on the defender's ability.
   * @param state the current battle state
   * @param move the move being used by the attacking Pokémon
   * @param ability the ability of the defending Pokémon
   * @return the damage multiplier as a Double
   */
  private def abilityDefenseMultiplier(state: BattleState, move: DamageMove, ability: Ability): Double =
    ability match
      case ThickFat if move.moveType == Fire =>
        println(s"[ThickFat] ${state.self.getActive.species.name} takes 50% less damage from Fire moves!")
        0.5
      case Levitate if move.category == Physical =>
        println(s"[Levitate] ${state.self.getActive.species.name} is immune to Physical moves!")
        0.0
      case _ => 1.0

  /**
   * Calculates the damage multiplier based on the attacker's ability.
   * @param state the current battle state
   * @param move the move being used by the attacking Pokémon
   * @param ability the ability of the attacking Pokémon
   * @return the damage multiplier as a Double
   */
  private def abilityAttackMultiplier(state: BattleState, move: DamageMove, ability: Ability): Double = {
    val attacker = state.self.getActive
    val lowHp = attacker.currentHp <= attacker.maxHp / 3

    ability match
      case Blaze if lowHp && move.moveType == Fire =>
        println(s"[Blaze] ${state.self.getActive.species.name} is using Blaze! Fire moves are boosted!")
        1.5
      case Torrent if lowHp && move.moveType == Water =>
        println(s"[Torrent] ${state.self.getActive.species.name} is using Torrent! Water moves are boosted!")
        1.5
      case Overgrow if lowHp && move.moveType == Grass =>
        println(s"[Overgrow] ${state.self.getActive.species.name} is using Overgrow! Grass moves are boosted!")
        1.5
      case SolarPower
        if state.weather == HeavySunlight
          && move.category == DamageMoveCategory.Special =>
       println(s"[SolarPower] ${state.self.getActive.species.name} 's special moves are boosted in Heavy Sunlight!")
        1.3
      case Ability.FlashFire
        if state.flags.selfFlashFireActive && move.moveType == Fire =>
          println(s"[FlashFire] ${state.self.getActive.species.name} 's Fire moves are boosted due to Flash Fire!")
          1.3
      case DroughtAura if move.moveType == Fire =>
        println(s"[DroughtAura] ${state.self.getActive.species.name} boosts Fire moves by 10%!")
        1.1
      case Guts if attacker.statusCondition.isDefined
          && move.category == Physical =>
        println(s"[Guts] ${state.self.getActive.species.name} boosts its Physical Attack by 30% when it has a status condition!")
        val burnCompensation = if attacker.statusCondition.contains(Burned) then 2.0 else 1.0
        1.3 * burnCompensation
      case _ => 1.0
  }

  def attackerMultiplier(state: BattleState, move: DamageMove): Double =
    val attacker = state.self.getActive
    val abilities = allAbilities(attacker.species.abilitySlot)
    abilities.map(abilityAttackMultiplier(state, move, _)).product
    
  def defenderMultiplier(state: BattleState, move: DamageMove): Double =
    val defender = state.opponent.getActive
    val abilities = allAbilities(defender.species.abilitySlot)
    abilities.map(abilityDefenseMultiplier(state, move, _)).product
    
  
    