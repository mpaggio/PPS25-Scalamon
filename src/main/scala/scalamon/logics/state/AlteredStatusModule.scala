package scalamon.logics.state

import scalamon.domain.moves.AlteredStatus
import scalamon.domain.moves.AlteredStatus.*
import scalamon.domain.moves.AlteredStatusUtility.*
import scalamon.domain.moves.Accuracy.*
import scalamon.domain.moves.Accuracy.given
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.StateTransformerModuleImpl.StateTransformer

/**
 * Logic module responsible for the lifecycle and combat effects of [[AlteredStatus]].
 *
 * This module implements the mechanics of status condition.
 * It leverages extension methods to enrich the domain enum with behavioral logic
 * and uses the StateTransformer to evolve the battle state without side effects.
 */
object AlteredStatusModule:

  extension (status: AlteredStatus)
    /**
     * Determines if the current status prevents the Pokémon from executing a move.
     *
     * Rules:
     * - [[Sleeping]] and [[Charging]] always prevent movement.
     * - [[Frozen]] and [[Paralyzed]] have a probabilistic chance of failure.
     * - Other statuses do not restrict movement.
     *
     * @return True if the Pokémon can act, false otherwise.
     */
    def canMove: Boolean = status match
      case Sleeping(_) | Charging(_) => false
      case Frozen => accuracyFromPercent(freezeThawingChance).test
      case Paralyzed => !accuracyFromPercent(paralysisFailureChance).test
      case _ => true

    /**
     * Check if the status causes the Pokémon to hit itself instead of the target.
     * Specifically used for the [[Confused]] status logic.
     *
     * @return True if a self-hit occurs, false otherwise.
     */
    def isSelfHitting: Boolean = status match
      case Confused(_) => accuracyFromPercent(confusionSelfHitChance).test
      case _ => false

    /**
     * Creates a [[StateTransformer]] that applies the end-of-turn or recurring
     * effects of a status condition.
     *
     * Logic implemented:
     * - [[Burned]] and [[Poisoned]] inflict damage over time (respecting a specific protection).
     * - [[Sleep]], [[Confusion]] and [[Charge]] decrement their internal turn counter. When
     *   it reaches 1, the condition is removed from the Pokémon's state.
     * - [[Burned]] and [[Poisoned]] inflict damage over time (respecting a specific protection).
     *
     * @return A function that transforms a [[BattleState]] into its next version.
     */
    def applyCondition: StateTransformer = battleState => status match
      case Burned | Poisoned =>
        if battleState.flags.selfMagicGuardActive then battleState
        else
          val a = battleState.self.getActive
          val damageAmount = a.species.baseStats.hp.toInt / statusDamageDivisor
          self(active(takeDamage(damageAmount)))(battleState)
      case Sleeping(turns) =>
        if turns > 1 then
          self(active(addStatus(Sleeping(turns - 1))))(battleState)
        else removeCondition(Sleeping(turns - 1))(battleState)
      case Confused(turns) =>
        if turns > 1 then
          self(active(addStatus(Confused(turns - 1))))(battleState)
        else removeCondition(Confused(turns - 1))(battleState)
      case Charging(turns) =>
        if turns > 1 then
          self(active(addStatus(Charging(turns - 1))))(battleState)
        else removeCondition(Charging(turns - 1))(battleState)
      case _ => battleState

  /**
   * Internal helper to generate a state transformation that removes a specific status.
   *
   * @param status The status condition to be removed from the active Pokémon.
   * @return A [[StateTransformer]] performing the removal logic.
   */
  private def removeCondition(status: AlteredStatus): StateTransformer = self(active(removeStatus(status)))