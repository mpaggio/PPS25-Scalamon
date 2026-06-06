package scalamon.domain.moves

object Power:
  opaque type Power = Int

  def fromInt(power: Int): Power =
    require(power > 0 && power <= 250, s"Invalid power: $power")
    power

  def fromDouble(power: Double): Power =
    fromInt(power.toInt)

  extension (power: Power)
    def asString: String = s"Power: $power"
    def asInt: Int = power
    def asDouble: Double = power.toDouble