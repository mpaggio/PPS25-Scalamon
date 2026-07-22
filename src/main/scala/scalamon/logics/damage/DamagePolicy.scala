package scalamon.logics.damage

/**
 * Defines the damage levels for the battle, which determines how much damage moves will deal.
 */
trait DamagePolicy:
  def multiplier: Double

/**
 * Companion object for the DamagePolicy trait, providing predefined difficulty levels.
 */
object DamagePolicy:
  /**
   * Easy difficulty level, where moves deal 10% of their base damage.
   */
  object Easy:
    given DamagePolicy with
      override def multiplier: Double = 0.1

  /**
   * Medium difficulty level, where moves deal 20% of their base damage.
   */
  object Medium:
    given DamagePolicy with
      override def multiplier: Double = 0.2

  /**
   * Hard difficulty level, where moves deal 30% of their base damage.
   */
  object Hard:
    given DamagePolicy with
      override def multiplier: Double = 0.3