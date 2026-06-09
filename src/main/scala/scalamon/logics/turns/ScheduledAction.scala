package scalamon.logics.turns

opaque type Speed = Int
object Speed:
  def apply(value: Int): Speed = value

  extension (speed: Speed)
    def value: Int = speed

final case class ScheduledAction(action: BattleAction, speed: Speed)