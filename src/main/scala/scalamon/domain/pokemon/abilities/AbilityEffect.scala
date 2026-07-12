package scalamon.domain.pokemon.abilities

import scalamon.logics.state.BattleStateImpl.BattleState

/**
 * Represents the effect of an ability in the battle state.
 * It is an opaque type that encapsulates a function transforming the BattleState.
 */
opaque type AbilityEffect = BattleState => BattleState

/**
 * Companion object for the AbilityEffect type.
 * Provides a method to create an AbilityEffect from a function.
 */
object AbilityEffect:

  /**
   * Constructor for AbilityEffect from a function that transforms the BattleState.
   * @param f a function that takes a BattleState and returns a modified BattleState
   * @return an AbilityEffect encapsulating the provided function
   */
  def apply(f: BattleState => BattleState): AbilityEffect = f

  /**
   * Extension method to execute the AbilityEffect on a given BattleState.
   * @return the new BattleState after applying the AbilityEffect
   */
  extension (e: AbilityEffect)
    def run(state: BattleState): BattleState = e(state)

/**
 * Couples an ability with its trigger and the effect it applies.
 * Fundamental unit used by the DSL
 * @param ability the ability that is being defined
 * @param trigger the moment when the ability is triggered
 * @param effect the effect of the ability when it is triggered
 */
case class AbilityDefinition(
  ability: Ability,
  trigger: AbilityTrigger,
  effect: AbilityEffect
)