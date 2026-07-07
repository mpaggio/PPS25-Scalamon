package scalamon.domain.moves

/**
 * Represents the various status condition that can affect a Pokémon during battle.
 *
 * This enumeration defines an Algebric Data Type that distinguishes between:
 * - Persistent statuses: Burned, Poisoned, Paralyzed, Frozen.
 * - Volatile statuses: Confused, Sleeping, Charging.
 * The volatile statuses includes an internal state representing the remaining number of turns.
 *
 * The behavioral logic for the statuses (such as movement restriction or damage over time) is
 * decoupled from this definition and encapsulated within the logic layer (see [[AlteredStatusModule]])
 * to maintain a clean separation of concerns.
 */
enum AlteredStatus:
  case Burned, Poisoned, Paralyzed, Frozen
  case Confused(turns: Int)
  case Sleeping(turns: Int)
  case Charging(turns: Int)

/**
 * Utility module providing constants and factory methods for status-related calculations.
 */
object AlteredStatusUtility:
  import scala.util.Random

  /** The divisor used to calculate damage over time. */
  val statusDamageDivisor = 8
  /** The probability percentage that a paralyzed Pokémon fails to move. */
  val paralysisFailureChance = 25
  /** The probability percentage that a confused Pokémon hits itself. */
  val confusionSelfHitChance = 50
  /** The probability percentage that a frozen Pokémon thaws. */
  val freezeThawingChance = 10
  /** The minimum duration for the Sleep status. */
  val minSleepTurns = 1
  /** The maximum duration for the Sleep status. */
  val maxSleepTurns = 4
  /** The minimum duration for the Confusion status. */
  val minConfusionTurns = 2
  /** The maximum duration for the Confusion status. */
  val maxConfusionTurns = 5

  /**
   * Generates a randomized duration for the [[Sleeping]] status.
   * Uses the defined bounds to ensure consistent game balancing.
   *
   * @return A random integer between [[minSleepTurns]] and [[maxSleepTurns]].
   */
  def getSleepTurns: Int =
    Random.nextInt(maxSleepTurns - minSleepTurns + 1) + minSleepTurns

  /**
   * Generates a randomized duration for the [[Confusion]] status.
   * Uses the defined bounds to ensure consistent game balancing.
   *
   * @return A random integer between [[minConfusionTurns]] and [[maxConfusionTurns]].
   */
  def getConfusionTurns: Int =
    Random.nextInt(maxConfusionTurns - minConfusionTurns + 1) + minConfusionTurns