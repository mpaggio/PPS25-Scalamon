package scalamon.logics.state

import scalamon.domain.moves.AlteredStatus
import scalamon.domain.moves.AlteredStatus.*
import scalamon.domain.moves.AlteredStatusUtility.*
import scalamon.domain.moves.Accuracy.*
import scalamon.logics.log.BattleLogger
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
    def canMove(using roll: ProbabilityRoll): Boolean = status match
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
    def isSelfHitting(using roll: ProbabilityRoll): Boolean = status match
      case Confused(_) => accuracyFromPercent(confusionSelfHitChance).test
      case _ => false

    /**
     * Creates a [[StateTransformer]] that applies the end-of-turn or recurring
     * effects of a status condition.
     *
     * Logic implemented:
     * - [[Burned]] and [[Poisoned]] inflict damage over time (respecting a specific protection).
     * - [[Sleep]], [[Confusion]] and [[Charge]] decrement their internal turn counter. When
     *   it reaches 0, the condition is removed from the Pokémon's state at the end of the turn.
     * - [[Burned]] and [[Poisoned]] inflict damage over time (respecting a specific protection).
     *
     * @return A function that transforms a [[BattleState]] into its next version.
     */
    def applyCondition(using roll: ProbabilityRoll): StateTransformer = battleState => status match
      case Burned | Poisoned =>
        if battleState.self.flags.magicGuardActive then battleState
        else
          val a = battleState.self.getActive
          val damageAmount = a.species.baseStats.hp.toInt / statusDamageDivisor
          val damageState = self(active(takeDamage(damageAmount)))(battleState)
          updateLogs(BattleLogger.logStatusDamage(a, status, damageAmount))(damageState)
      case Sleeping(turns) =>
        val a = battleState.self.getActive
        if turns > 0 then
          val nextState = self(active(addStatus(Sleeping(turns - 1))))(battleState)
          updateLogs(BattleLogger.logStatusContinues(a, Sleeping(turns - 1)))(nextState)
        else
          val nextState = removeCondition(Sleeping(turns - 1))(battleState)
          updateLogs(BattleLogger.logStatusEnded(a, Sleeping(turns)))(nextState)
      case Confused(turns) =>
        val a = battleState.self.getActive
        if turns > 0 then
          val nextState = self(active(addStatus(Confused(turns - 1))))(battleState)
          updateLogs(BattleLogger.logStatusContinues(a, Confused(turns - 1)))(nextState)
        else
          val nextState = removeCondition(Confused(turns - 1))(battleState)
          updateLogs(BattleLogger.logStatusEnded(a, Confused(turns)))(nextState)
      case Charging(turns) =>
        val a = battleState.self.getActive
        if turns > 0 then
          val nextState = self(active(addStatus(Charging(turns - 1))))(battleState)
          updateLogs(BattleLogger.logStatusContinues(a, Charging(turns - 1)))(nextState)
        else
          val nextState = removeCondition(Charging(turns - 1))(battleState)
          updateLogs(BattleLogger.logStatusEnded(a, Charging(turns)))(nextState)
      case _ => battleState

  /**
   * Internal helper to generate a state transformation that removes a specific status.
   *
   * @param status The status condition to be removed from the active Pokémon.
   * @return A [[StateTransformer]] performing the removal logic.
   */
  private def removeCondition(status: AlteredStatus): StateTransformer = self(active(removeStatus(status)))