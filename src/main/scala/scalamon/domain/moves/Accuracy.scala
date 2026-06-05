package scalamon.domain.moves

object Accuracy:
  opaque type Accuracy = Int

  def fromPercent(accuracy: Int): Accuracy =
    require(accuracy >= 0 && accuracy <= 100, s"Invalid accuracy: $accuracy")
    accuracy

  def fromRatio(accuracy: Double): Accuracy =
    fromPercent((accuracy * 100.0).toInt)

  extension (a: Accuracy)
    def asString: String = s"Accuracy: $a%"
