package scalamon.logics.state

trait DamagePolicy:
  def multiplier: Double

object DamagePolicy:
  object Easy:
    given DamagePolicy with
      override def multiplier: Double = 0.85

  object Medium:
    given DamagePolicy with
      override def multiplier: Double = 1.0

  object Hard:
    given DamagePolicy with
      override def multiplier: Double = 1.15