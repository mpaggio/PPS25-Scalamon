package scalamon.domain.moves

import Accuracy.*
import scalamon.domain.pokemon.statistics.StatADT.*

trait AlteredStatus

object Burned extends AlteredStatus
object Paralyzed extends AlteredStatus
object Poisoned extends AlteredStatus
object Sleeping extends AlteredStatus
object Frozen extends AlteredStatus
object Confused extends AlteredStatus

enum MoveEffect:
  case AlteredState(status: AlteredStatus, probability: Accuracy)
  case StatChange(stat: StatKind, stages: Int, probability: Accuracy)
  case CriticalMultiplier(multiplier: Int)
  case Heal(percentage: Int)
  case Recoil(percentage: Int)
  case Recharge(recharges: Int)

object MoveEffectDSL:

  import MoveEffect.*

  object Effect:
    infix def applying(alteredStatus: AlteredStatus) = AlteredStatusEffectBuilder(alteredStatus)
    infix def changing(stat: StatKind) = StatChangeEffectBuilder(stat)
    infix def healing(percent: Int) = Heal(percent)
    infix def recoil(percent: Int) = Recoil(percent)
    infix def recharging(recharges: Int) = Recharge(recharges)
    infix def multiplyingCriticalBy(value: Int) = CriticalMultiplier(value)

  case class AlteredStatusEffectBuilder(status: AlteredStatus):
    infix def withProbability(probability: Int): MoveEffect =
      AlteredState(status, accuracyFromPercent(probability))

    infix def withProbability(probability: Double): MoveEffect =
      AlteredState(status, accuracyFromRatio(probability / 100.0))

  case class StatChangeEffectBuilder(stat: StatKind):
    infix def by(stages: Int): StatChangeProbabilityBuilder =
      StatChangeProbabilityBuilder(stat, stages)

  case class StatChangeProbabilityBuilder(stat: StatKind, stages: Int):
    infix def withProbability(probability: Int): MoveEffect =
      StatChange(stat, stages, accuracyFromPercent(probability))

    infix def withProbability(probability: Double): MoveEffect =
      StatChange(stat, stages, accuracyFromRatio(probability / 100.0))