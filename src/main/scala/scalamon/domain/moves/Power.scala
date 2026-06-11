package scalamon.domain.moves

/**
 * Represents the base power of a move.
 *
 * This is an opaque type that wraps an `Int`, ensuring that power values
 * are validated and constrained within the domain rules.
 *
 * Valid range: (0, 250].
 */
object Power:
  opaque type Power = Int

  /**
   * Create a `Power` from an Int value.
   *
   * @param power integer power value in range (0, 250].
   * @return a validated Power instance.
   */
  def powerFromInt(power: Int): Power =
    powerFromDouble(power)

  /**
   * Create a `Power` from a Double value.
   * Only whole numbers are accepted.
   *
   * @param power double power value in range (0.0, 250.0].
   * @return a validated Power instance.
   * @throws IllegalArgumentException if value is not whole or out of range.
   */
  def powerFromDouble(power: Double): Power =
    require(power.isWhole && power > 0.0 && power <= 250.0, s"Invalid power: $power")
    power.toInt

  /**
   * Extension methods for working with Power values.
   */
  extension (power: Power)

    /**
     * @return human-readable string representation (e.g. "Power: 70").
     */
    def asString: String = s"Power: $power"

    /**
     * @return power as an Int value.
     */
    def asInt: Int = power

    /**
     * @return power as a Double value.
     */
    def asDouble: Double = power.toDouble