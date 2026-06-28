package scalamon.domain.pokemon.abilities

import scalamon.logics.state.BattleStateImpl.BattleState

/**
 * Passive Effect: Transformation of the battle state.
 * The abilities that modifies the damage calculated do not use AbilityEffect,
 * but AbilityDamageModifier instead.
 */
opaque type AbilityEffect = BattleState => BattleState

object AbilityEffect:
  def apply(f: BattleState => BattleState): AbilityEffect = f

  extension (e: AbilityEffect)
    def run(state: BattleState): BattleState = e(state)

/**
 * Couples an ability with its trigger and effect.
 * Fundamental unit of the DSL
 * @param ability the ability that is being defined
 * @param trigger the moment when the ability is triggered
 * @param effect the effect of the ability when it is triggered
 */
case class AbilityDefinition(
  ability: Ability,
  trigger: AbilityTrigger,
  effect: AbilityEffect
)