package scalamon.domain.moves

import Accuracy.*
import Accuracy.given
import AlteredStatus.*
import scalamon.domain.pokemon.statistics.StatADT.*
import scalamon.domain.pokemon.abilities.Target
import scalamon.domain.pokemon.abilities.Target.*
import scalamon.logics.state.StateTransformerModuleImpl.*

/**
 * Represents the possible targets of an effect
 */


/**
 * Represents all the possible effects that a move can have.
 *
 * These effects are applied in addition to (or instead of) damage
 * and define additional battle mechanisms such as:
 * - Status infliction.
 * - Stat modifications.
 * - Healing and recoil.
 * - Critical hit multipliers.
 * - Forced recharge turns.
 */
trait MoveEffect:
  def executeEffect: StateTransformer

case class ComposableEffect(transformer: StateTransformer) extends MoveEffect:
  override def executeEffect: StateTransformer = transformer

/**
 * Applies a status condition to the target with a given probability.
 * Example: paralysis, burn, poison, sleep, freeze, confusion.
 */
case class AlteredState(statusFactory: () => AlteredStatus, probability: Accuracy) extends MoveEffect:
  override def executeEffect: StateTransformer =
    if probability.test then
      val statusToApply = statusFactory()
      opponent(active(addStatus(statusToApply)))
    else
      identity

/**
 * Modifies one of the target's stats by a number of stages.
 * Positives values increase stats, negative values decrease them.
 */
case class StatChange(modifier: StatsState => StatsState, effectTarget: Target, probability: Accuracy) extends MoveEffect:
  override def executeEffect: StateTransformer =
    if probability.test then effectTarget match
      case Target.Self => self(active(modifyStats(modifier)))
      case Target.Opponent => opponent(active(modifyStats(modifier)))
    else
      identity

/**
 * Increases the critical hit multiplier of the move.
 * Higher values increase critical hits.
 */
case class CriticalMultiplier(multiplier: Int) extends MoveEffect:
  override def executeEffect: StateTransformer = battleState => battleState

/**
 * Restores a percentage of the user's HP.
 *
 * @param percentage amount of HP restored (0-100).
 */
case class Heal(percentage: Int) extends MoveEffect:
  override def executeEffect: StateTransformer = battleState =>
    val activePokemon = battleState.self.getActive
    val healAmount = (percentage * activePokemon.species.baseStats.hp.toInt) / 100
    self(active(heal(healAmount)))(battleState)

/**
 * Deals damage to the user as recoil after the move is executed.
 *
 * @param percentage percentage of damage reflected back to the user (0-100).
 */
case class Recoil(percentage: Int) extends MoveEffect:
  override def executeEffect: StateTransformer = battleState =>
    val activePokemon = battleState.self.getActive
    val recoilAmount = (percentage * activePokemon.species.baseStats.hp.toInt) / 100
    self(active(takeDamage(recoilAmount)))(battleState)

/**
 * Forces the user to spend a number of turns recharging
 * after using the move.
 *
 * @param recharges number of turns required to recharge.
 */
case class Recharge(recharges: Int) extends MoveEffect:
  override def executeEffect: StateTransformer = self(active(addStatus(Charging(recharges))))

/**
 * Domain Specific Language (DSL) for constructing MoveEffect values.
 *
 * This DSL provides a fluent API to define secondary move effects
 * in a readable and expressive way, avoiding direct use of constructors.
 *
 * Example usage:
 * {{{
 * Effect applying Paralyzed withProbability 30
 * Effect changing Attack by -1 withProbability 20
 * Effect healing 40
 * Effect recoil 20
 * Effect recharging 1
 * Effect multiplyingCriticalBy 2
 * }}}
 */
object MoveEffectDSL:

  /**
   * Entry point for the MoveEffect DSL.
   * Provides the starting keyword for building effects.
   * Exposes keywords used to build move effects (readable verbs in DSL).
   */
  object Effect:
    infix def transformingBy(transformer: StateTransformer) = ComposableEffect(transformer)
    infix def applying(status: => AlteredStatus) = AlteredStatusEffectBuilder(() => status)
    infix def changing(modifier: StatsState => StatsState) = StatChangeEffectBuilder(modifier)
    infix def healing(percent: Int) = Heal(percent)
    infix def recoil(percent: Int) = Recoil(percent)
    infix def recharging(recharges: Int) = Recharge(recharges)
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
   * Represents the second step in the creation: define probability.
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