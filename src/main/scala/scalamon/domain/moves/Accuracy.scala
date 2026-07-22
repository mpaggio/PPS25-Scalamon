package scalamon.domain.moves

import scala.util.Random

/**
 * Represents the accuracy of a move as a value between 0% and 100%.
 *
 * It utilizes an opaque type to wrap an ⁠ Int ⁠, ensuring that accuracy values
 * are treated as a distinct domain concept, rather than mere integers.
 * This prevents the primitive obsession and guarantees that all values in the
 * system are validated and clamped between 0% and 100%.
 */
object Accuracy:
  /** The opaque representation of accuracy as a percentage. */
  opaque type Accuracy = Int
  /** A functional strategy for generating a probability roll. */
  type ProbabilityRoll = () => Int

  /** Default implementation of the probability roll, provided as a given. */
  given defaultRoll: ProbabilityRoll = () => Random.nextInt(100) + 1

  /**
   * Factory method to create an ⁠ Accuracy ⁠ instance from a percentage value.
   *
   * @param accuracy percentage value in range [0, 100].
   * @return a validated Accuracy instance.
   */
  def accuracyFromPercent(accuracy: Int): Accuracy =
    accuracyFromRatio(accuracy.toDouble / 100.0)

  /**
   * Factory method to create an ⁠ Accuracy ⁠ instance from a ratio value.
   *
   * @param accuracy ratio in range [0.0, 1.0].
   * @return a validated Accuracy instance.
   * @throws IllegalArgumentException if the value is outside [0.0, 1.0].
   */
  def accuracyFromRatio(accuracy: Double): Accuracy =
    require(accuracy >= 0.0 && accuracy <= 1.0, s"Invalid accuracy: $accuracy")
    (accuracy * 100.0).toInt

  /** Internal utility to keep accuracy within domain boundaries. */
  private def clamp(value: Int): Accuracy = math.max(0, math.min(100, value))

  /**
   * Extension methods for working with Accuracy values.
   */
  extension (accuracy: Accuracy)

    /**
     * @return A human-readable string representation (e.g. "Accuracy: 70%").
     */
    def asString: String = s"Accuracy: $accuracy%"

    /**
     * @return The accuracy as Int percentage value (0 - 100).
     */
    def asInt: Int = accuracy

    /**
     * @return The accuracy as Double percentage value (0.0 - 100.0).
     */
    def asDouble: Double = accuracy.toDouble


    /**
     * Executes a probabilistic test to determine if an action succeeds.
     *
     * Uses a Contextual Abstraction to summon a [[ProbabilityRoll]].
     * This allows for deterministic testing by injecting a custom roll strategy.
     *
     * @param roll The strategy used to generate the random roll.
     * @return True if the roll is less than or equal to the accuracy, false otherwise.
     */
    def test(using roll: ProbabilityRoll): Boolean = roll() <= accuracy.asInt

    /**
     * Increases accuracy by a fixed percentage value.
     *
     * @param value The value to be added to the current accuracy.
     * @return The resulting accuracy value (not clamped).
     */
    infix def +(value: Int): Accuracy = clamp(accuracy + value)

    /**
     * Decreases accuracy by a fixed percentage value.
     *
     * @param value The value to be decreased from the current accuracy.
     * @return The resulting accuracy value (not clamped).
     */
    infix def -(value: Int): Accuracy = clamp(accuracy - value)

    /**
     * Multiply accuracy by a fixed double value.
     *
     * @param value The multiplier factor to be multiplied to current accuracy.
     * @return The resulting accuracy value (not clamped).
     */
    infix def *(value: Double): Accuracy = clamp((accuracy.toDouble * value).toInt)