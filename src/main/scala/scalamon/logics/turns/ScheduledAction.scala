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

/**
 * A battle action scheduled together with the speed used to resolve its order.
 *
 * @param action
 *   the battle action to execute
 * @param speed
 *   the speed associated with the action
 */
final case class ScheduledAction(action: BattleAction, speed: Speed)