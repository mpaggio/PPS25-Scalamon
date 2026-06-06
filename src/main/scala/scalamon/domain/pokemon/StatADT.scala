package scalamon.domain.pokemon

object StatADT:
  
  opaque type Stat = Int
  
  def fromInt(value: Int): Stat =
    require (value > 0, "Stat must be a value greater than 0")
    value
    
  extension (stat: Stat)
    def toInt: Int = stat