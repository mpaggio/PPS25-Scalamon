package scalamon.logics.turns

/**
 * Opaque speed value used to schedule battle actions.
 */
opaque type Speed = Int
object Speed:
  /**
   * Creates a [[Speed]] from its integer representation.
   *
   * @param value
   * the raw speed value
   * @return
   * a speed wrapper
   */
  def apply(value: Int): Speed = value

  extension (speed: Speed)
    /**
     * Extracts the underlying integer value of this speed.
     *
     * @return
     *   the raw speed value
     */
    def value: Int = speed
    def >=(other: Speed): Boolean = speed >= other.value
