package scalamon.domain.moves

object PowerPoints:
  opaque type PP = Int

  def powerPointsFromInt(pp: Int): PP =
    powerPointsFromDouble(pp)

  def powerPointsFromDouble(pp: Double): PP =
    require(pp > 0.0 && pp <= 64.0, s"Invalid PP: $pp")
    pp.toInt

  extension (pp: PP)
    def asString: String = s"PP: $pp"
    def asInt: Int = pp
    def asDouble: Double = pp.toDouble
