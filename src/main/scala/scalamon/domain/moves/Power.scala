package scalamon.domain.moves

object Power:
  opaque type Power = Int

  def powerFromInt(power: Int): Power =
    powerFromDouble(power)

  def powerFromDouble(power: Double): Power =
    require(power > 0.0 && power <= 250.0, s"Invalid power: $power")
    power.toInt

  extension (power: Power)
    def asString: String = s"Power: $power"
    def asInt: Int = power
    def asDouble: Double = power.toDouble