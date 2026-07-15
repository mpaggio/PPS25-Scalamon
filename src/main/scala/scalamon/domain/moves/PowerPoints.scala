package scalamon.domain.moves

/**
 * Represents the power-points (PP) of a move.
 *
 * This is an opaque type that wraps an `Int`, ensuring that power points
 * values are validated and constrained within the domain rules.
 *
 * Valid range: (0, 64].
 */
object PowerPoints:
  opaque type PP = Int

  /**
   * Create a `PP` from an Int value.
   *
   * @param pp integer power points value in range (0, 64].
   * @return a validated PP instance.
   */
  def powerPointsFromInt(pp: Int): PP =
    powerPointsFromDouble(pp)

  /**
   * Create a `PP` from a Double value.
   * Only whole numbers are accepted.
   *
   * @param pp integer power points value in range (0.0, 64.0].
   * @return a validated PP instance.
   * @throws IllegalArgumentException if value is not whole or out of range.
   */
  def powerPointsFromDouble(pp: Double): PP =
    require(pp.isWhole && pp > 0.0 && pp <= 64.0, s"Invalid PP: $pp")
    pp.toInt

  /**
   * Extension methods for working with PP values.
   */
  extension (pp: PP)

    /**
     * @return human-readable string representation (e.g. "PP: 40").
     */
    def asString: String = s"PP: $pp"

    /**
     * @return power points as an Int value.
     */
    def asInt: Int = pp

    /**
     * @return power points as a Double value.
     */
    def asDouble: Double = pp.toDouble
