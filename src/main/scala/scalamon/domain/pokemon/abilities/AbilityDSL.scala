package scalamon.domain.pokemon.abilities

import scalamon.logics.state.BattleStateModuleImpl.BattleState

/**
 * Declarative DSL for the definition of passive abilities.
 * The DSL allows to define abilities in a concise and readable way.
 */
object AbilityDSL:

  /**
   * Top level constructor: collects all the ability definitions and groups them in a map.
   * The same ability can have multiple definitions (more triggers)
   * @param defs the ability definitions to be grouped
   * @return a map of abilities to their definitions
   */
  def AbilityBook(defs: AbilityDefinition*): Map[Ability, List[AbilityDefinition]] =
    defs.groupBy(_.ability).map((k, v) => k -> v.toList)

  /**
   * Builder Step 1: captures the trigger of the ability.
   * @param trigger the moment when the ability is triggered
   */
  case class OnTrigger(trigger: AbilityTrigger):
    infix def define(ability: Ability): AbilityBuilderStep =
      AbilityBuilderStep(ability, trigger)

  /**
   * Builder Step 2: captures the ability and produces the definition with the effect.
   * @param ability the ability that is being defined
   * @param trigger the moment when the ability is triggered
   */
  case class AbilityBuilderStep(ability: Ability, trigger: AbilityTrigger):
    infix def as(effect: BattleState => BattleState): AbilityDefinition =
      AbilityDefinition(ability, trigger, AbilityEffect(effect))