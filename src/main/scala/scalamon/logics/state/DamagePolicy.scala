package scalamon.logics.state

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