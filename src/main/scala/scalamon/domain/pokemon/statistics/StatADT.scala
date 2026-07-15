package scalamon.domain.pokemon.statistics

/**
 * Module defining the statistic value as an opaque type.
 * Centralizes the validation that every statistic must be a positive integer.
 */
object StatADT:
  
  opaque type Stat = Int
  
  def fromInt(value: Int): Stat =
    require (value > 0, "Stat must be a value greater than 0")
    value
    
  extension (stat: Stat)
    def toInt: Int = stat

  enum StatKind:
    case Hp
    case Attack
    case Defense
    case SpecialAttack
    case SpecialDefense
    case Speed