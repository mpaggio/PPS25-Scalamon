package scalamon.domain.moves

object Accuracy:
  opaque type Accuracy = Int

  def accuracyFromPercent(accuracy: Int): Accuracy =
    accuracyFromRatio(accuracy.toDouble / 100.0)
    accuracy

  def accuracyFromRatio(accuracy: Double): Accuracy =
    require(accuracy >= 0.0 && accuracy <= 1.0, s"Invalid accuracy: $accuracy")
    (accuracy * 100.0).toInt

  extension (accuracy: Accuracy)
    def asString: String = s"Accuracy: $accuracy%"
    def asInt: Int = accuracy
    def asDouble: Double = accuracy.toDouble