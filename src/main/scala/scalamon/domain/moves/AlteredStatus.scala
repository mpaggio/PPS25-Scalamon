package scalamon.domain.moves

enum AlteredStatus:
  case Burned, Poisoned, Paralyzed, Frozen
  case Confused(turns: Int)
  case Sleeping(turns: Int)
  case Charging(turns: Int)

object AlteredStatusUtility:
  import scala.util.Random

  val statusDamageDivisor = 8
  val paralysisFailureChance = 25
  val confusionSelfHitChance = 50
  val freezeThawingChance = 10
  val minSleepTurns = 1
  val maxSleepTurns = 4
  val minConfusionTurns = 2
  val maxConfusionTurns = 5

  def getSleepTurns: Int =
    Random.nextInt(maxSleepTurns - minSleepTurns + 1) + minSleepTurns

  def getConfusionTurns: Int =
    Random.nextInt(maxConfusionTurns - minConfusionTurns + 1) + minConfusionTurns