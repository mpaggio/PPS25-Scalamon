package scalamon.domain.moves

import Accuracy.*
import Accuracy.given
import scalamon.domain.alteredStatus.AlteredStatus.*
import scalamon.domain.pokemon.statistics.StatADT.*
import scalamon.domain.pokemon.abilities.Target
import scalamon.domain.pokemon.abilities.Target.*
import scalamon.logics.log.BattleLogger
import scalamon.logics.state.StateTransformerModuleImpl.*

/**
 * Represents all the possible effects that a move can have.
 *
 * Represents a strategy for evolving the battle state through non-damaging consequences.
 * Each effect implements a StateTransformer, returning a [[StateTransformer]] function
 * that maps a [[BattleState]] to its next version.
 */
trait MoveEffect:
  /**
   * Produces the state transformation logic associated with this side effect.
   *
   * @return A pure function that evolves the battle state.
   */
  def executeEffect: StateTransformer

/**
 * A wrapper that encapsulates an arbitrary state transformation.
 *
 * This class enables effect composition, allowing to combine multiple simple
 * transformations into a single complex battle event.
 *
 * @param transformer The logic to be executed on the battle state.
 */
case class ComposableEffect(transformer: StateTransformer) extends MoveEffect:
  override def executeEffect: StateTransformer = transformer

/**
 * Applies a status condition to the target Pokémon based on a given probability.
 *
 * @param statusFactory A functional supplier that provides the status to apply.
 * @param probability The [[Accuracy]] value representing the chance of success.
 */
case class AlteredState(statusFactory: () => AlteredStatus, probability: Accuracy) extends MoveEffect:
  override def executeEffect: StateTransformer = battleState =>
    if probability.test then
      val statusToApply = statusFactory()
      val target = battleState.opponent.getActive
      val updatedState = opponent(active(addStatus(statusToApply)))(battleState)
      updateLogs(BattleLogger.logStatusInflicted(target, statusToApply))(updatedState)
    else
      battleState

/**
 * Modifies a specific statistic of the target by a number of stages.
 *
 * @param modifier A function that transforms the [[StatsState]].
 * @param effectTarget The active Pokémon targeted by the change (Self or Opponent).
 * @param probability The [[Accuracy]] representing the success rate of the modification.
 */
case class StatChange(modifier: StatsState => StatsState, effectTarget: Target, probability: Accuracy) extends MoveEffect:
  override def executeEffect: StateTransformer =
    if probability.test then effectTarget match
      case Target.Self => self(active(modifyStats(modifier)))
      case Target.Opponent => opponent(active(modifyStats(modifier)))
    else
      identity

/**
 * Increases the critical hit probability of the move.
 *
 * @param multiplier The multiplier value to be multiplied to standard critical attack probability.
 */
case class CriticalMultiplier(multiplier: Int) extends MoveEffect:
  override def executeEffect: StateTransformer = battleState => battleState

/**
 * Heals the user Pokémon by a percentage of its maximum HP.
 *
 * @param percentage The percentage of max HP to restore (range 0-100).
 */
case class Heal(percentage: Int) extends MoveEffect:
  override def executeEffect: StateTransformer = battleState =>
    val activePokemon = battleState.self.getActive
    val healAmount = (percentage * activePokemon.species.baseStats.hp.toInt) / 100
    self(active(heal(healAmount)))(battleState)

/**
 * Deals recoil damage to the user Pokémon as a penalty for using a powerful move.
 *
 * @param percentage The percentage of the user's max HP to be taken as damage.
 */
case class Recoil(percentage: Int) extends MoveEffect:
  override def executeEffect: StateTransformer = battleState =>
    val activePokemon = battleState.self.getActive
    val recoilAmount = (percentage * activePokemon.species.baseStats.hp.toInt) / 100
    self(active(takeDamage(recoilAmount)))(battleState)

/**
 * Forces the user to skip a number of turns recharging, after executing a high-power move.
 *
 * @param recharges The number of turns the Pokémon must remain in the Charging state.
 */
case class Recharge(recharges: Int) extends MoveEffect:
  override def executeEffect: StateTransformer = battleState =>
    val user = battleState.self.getActive
    val updatedState = self(active(addStatus(Charging(recharges))))(battleState)
    updateLogs(BattleLogger.logStatusInflicted(user, Charging(recharges)))(updatedState)

/**
 * Module providing a Domain Specific Language (DSL) for declarative effect construction.
 *
 * This DSL mitigates opacity by using human-readable verbs instead of direct constructor
 * calls, ensuring that move definitions in the database remain expressive and clean.
 */
object MoveEffectDSL:

  /**
   * Entry point for the MoveEffect DSL.
   * Provides the starting keyword for building effects.
   * Exposes keywords used to build move effects (readable verbs in DSL).
   */
  object Effect:
    /** Creates a custom composable effect from a transformer. */
    infix def transformingBy(transformer: StateTransformer) = ComposableEffect(transformer)
    /** Starts the definition of a status-applying effect. */
    infix def applying(status: => AlteredStatus) = AlteredStatusEffectBuilder(() => status)
    /** Starts the definition of a stat-modifying effect. */
    infix def changing(modifier: StatsState => StatsState) = StatChangeEffectBuilder(modifier)
    /** Creates a healing effect. */
    infix def healing(percent: Int) = Heal(percent)
    /** Creates a recoil effect. */
    infix def recoil(percent: Int) = Recoil(percent)
    /** Creates a recharge effect. */
    infix def recharging(recharges: Int) = Recharge(recharges)
    /** Creates a critical multiplier effect. */
    infix def multiplyingCriticalBy(value: Int) = CriticalMultiplier(value)

  /**
   * Builder for status-based effects.
   * Allows attaching a probability of applying a status condition.
   */
  case class AlteredStatusEffectBuilder(statusFactory: () => AlteredStatus):

    /**
     * Defines probability using Int percentage (0 - 100)
     */
    infix def withProbability(probability: Int): MoveEffect =
      AlteredState(statusFactory, accuracyFromPercent(probability))

    /**
     * Defines probability using Double percentage (0.0 - 100.0)
     */
    infix def withProbability(probability: Double): MoveEffect =
      AlteredState(statusFactory, accuracyFromRatio(probability / 100.0))

  /**
   * Builder for stat modification effects.
   * Allows attaching a probability of applying a stat change.
   */
  case class StatChangeEffectBuilder(modifier: StatsState => StatsState, target: Target = Target.Opponent):

    /**
     * Defines the target of the stat change effect
     */
    infix def ofTarget(effectTarget: Target): StatChangeEffectBuilder = copy(target = effectTarget)

    /**
     * Defines probability using Int percentage (0 - 100)
     */
    infix def withProbability(probability: Int): MoveEffect =
      StatChange(modifier, target, accuracyFromPercent(probability))

    /**
     * Defines probability using Double percentage (0.0 - 100.0)
     */
    infix def withProbability(probability: Double): MoveEffect =
      StatChange(modifier, target, accuracyFromRatio(probability / 100.0))