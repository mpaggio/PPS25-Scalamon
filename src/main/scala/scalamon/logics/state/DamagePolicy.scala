package scalamon.logics.state

/**
 * Defines the damage levels for the battle, which determines how much damage moves will deal.
 */
trait DamagePolicy:
  def multiplier: Double

object DamagePolicy:
  object Easy:
    given DamagePolicy with
      override def multiplier: Double = 0.1

  object Medium:
    given DamagePolicy with
      override def multiplier: Double = 0.2

  object Hard:
    given DamagePolicy with
      override def multiplier: Double = 0.3