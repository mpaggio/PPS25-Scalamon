package scalamon.domain.moves

import scala.util.Random

/**
 * Represents the accuracy of a move as a value between 0% and 100%.
 *
 * This is an opaque type that wraps an `Int`, ensuring that accuracy values
 * are always validated at construction time and cannot be created directly.
 *
 * Accuracy is internally stored as a percentage (0-100).
 */
object Accuracy:
  opaque type Accuracy = Int
  type ProbabilityRoll = () => Int
  given defaultRoll: ProbabilityRoll = () => Random.nextInt(100) + 1

  /**
   * Create an `Accuracy` from a percentage value.
   *
   * @param accuracy percentage value in range [0, 100].
   * @return a validated Accuracy instance.
   */
  def accuracyFromPercent(accuracy: Int): Accuracy =
    accuracyFromRatio(accuracy.toDouble / 100.0)

  /**
   * Create an `Accuracy` from a ratio value.
   *
   * @param accuracy ratio in range [0.0, 1.0].
   * @return a validated Accuracy instance.
   * @throws IllegalArgumentException if the value is outside [0.0, 1.0].
   */
  def accuracyFromRatio(accuracy: Double): Accuracy =
    require(accuracy >= 0.0 && accuracy <= 1.0, s"Invalid accuracy: $accuracy")
    (accuracy * 100.0).toInt

  /**
   * Extension methods for working with Accuracy values.
   */
  extension (accuracy: Accuracy)

    /**
     * @return human-readable string representation (e.g. "Accuracy: 70%").
     */
    def asString: String = s"Accuracy: $accuracy%"

    /**
     * @return accuracy as Int percentage value (0 - 100).
     */
    def asInt: Int = accuracy

    /**
     * @return accuracy as Double percentage value (0.0 - 100.0).
     */
    def asDouble: Double = accuracy.toDouble

    /**
     * @return true if the accuracy test has passed (roll result <= of accuracy Int value)
     */
    def test(using roll: ProbabilityRoll): Boolean = roll() <= accuracy.asInt