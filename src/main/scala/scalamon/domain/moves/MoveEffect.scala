package scalamon.domain.moves

import Accuracy.*
import scalamon.domain.pokemon.statistics.StatADT.*

trait AlteredStatus

object Burned extends AlteredStatus
object Paralyzed extends AlteredStatus
object Poisoned extends AlteredStatus
object Sleeping extends AlteredStatus
object Frozen extends AlteredStatus
object Confused extends AlteredStatus

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
enum MoveEffect:

  /**
   * Applies a satus condition to the target with a given probability.
   * Example: paralysis, burn, poison, sleep, freeze, confusion.
   */
  case AlteredState(status: AlteredStatus, probability: Accuracy)

  /**
   * Modifies one of the target's stats by a number of stages.
   * Positives values increase stats, negative values decrease them.
   */
  case StatChange(stat: StatKind, stages: Int, probability: Accuracy)

  /**
   * Increases the critical hit multiplier of the move.
   * Higher values increase critical hits.
   */
  case CriticalMultiplier(multiplier: Int)

  /**
   * Restores a percentage of the user's HP.
   *
   * @param percentage amount of HP restored (0-100).
   */
  case Heal(percentage: Int)

  /**
   * Deals damage to the user as recoil after the move is executed.
   *
   * @param percentage percentage of damage reflected back to the user (0-100).
   */
  case Recoil(percentage: Int)

  /**
   * Forces the user to spend a number of turns recharging
   * after using the move.
   *
   * @param recharges number of turns required to recharge.
   */
  case Recharge(recharges: Int)

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
  import MoveEffect.*

  /**
   * Entry point for the MoveEffect DSL.
   * Provides the starting keyword for building effects.
   * Exposes keywords used to build move effects (readable verbs in DSL).
   */
  object Effect:
    infix def applying(alteredStatus: AlteredStatus) = AlteredStatusEffectBuilder(alteredStatus)
    infix def changing(stat: StatKind) = StatChangeEffectBuilder(stat)
    infix def healing(percent: Int) = Heal(percent)
    infix def recoil(percent: Int) = Recoil(percent)
    infix def recharging(recharges: Int) = Recharge(recharges)
    infix def multiplyingCriticalBy(value: Int) = CriticalMultiplier(value)

  /**
   * Builder for status-based effects.
   * Allows attaching a probability of applying a status condition.
   */
  case class AlteredStatusEffectBuilder(status: AlteredStatus):

    /**
     * Defines probability using Int percentage (0 - 100)
     */
    infix def withProbability(probability: Int): MoveEffect =
      AlteredState(status, accuracyFromPercent(probability))

    /**
     * Defines probability using Double percentage (0.0 - 100.0)
     */
    infix def withProbability(probability: Double): MoveEffect =
      AlteredState(status, accuracyFromRatio(probability / 100.0))

  /**
   * Builder for stat modification effects.
   * Represents the first step in the creation: choose stat and then define stage.
   */
  case class StatChangeEffectBuilder(stat: StatKind):

    /**
     * Defines how many stages the stat will be modifed.
     */
    infix def by(stages: Int): StatChangeProbabilityBuilder =
      StatChangeProbabilityBuilder(stat, stages)

  /**
   * Builder for stat modification effects.
   * Represents the second step in the creation: define probability.
   */
  case class StatChangeProbabilityBuilder(stat: StatKind, stages: Int):

    /**
     * Defines probability using Int percentage (0 - 100)
     */
    infix def withProbability(probability: Int): MoveEffect =
      StatChange(stat, stages, accuracyFromPercent(probability))

    /**
     * Defines probability using Double percentage (0.0 - 100.0)
     */
    infix def withProbability(probability: Double): MoveEffect =
      StatChange(stat, stages, accuracyFromRatio(probability / 100.0))