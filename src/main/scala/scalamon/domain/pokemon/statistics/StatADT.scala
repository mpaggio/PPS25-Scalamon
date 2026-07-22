package scalamon.domain.pokemon.statistics

/**
 * Module defining the statistic value as an opaque type.
 * Centralizes the validation that every statistic must be a positive integer.
 */
object StatADT:
  
  opaque type Stat = Int

  /**
   * Factory method to create a Stat from an Int.
   * @param value the integer value to be converted into a Stat. Must be greater than 0.
   * @return a Stat instance representing the provided value.
   * @throws IllegalArgumentException if the provided value is not greater than 0.
   */
  def fromInt(value: Int): Stat =
    require (value > 0, "Stat must be a value greater than 0")
    value

  /**
   * Extension method to convert a Stat back to an Int.
   * @return the integer value represented by the Stat.
   */
  extension (stat: Stat)
    def toInt: Int = stat