package scalamon.domain.moves

import Accuracy.*
import scalamon.domain.pokemon.statistics.StatADT.*

trait AlteredStatus

object Burn extends AlteredStatus
object Paralysis extends AlteredStatus

enum MoveEffect:
  case AlteredState(status: AlteredStatus, probability: Accuracy)
  case StatChange(stat: StatKind, stages: Int, probability: Accuracy)
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

  case class AlteredStatusEffectBuilder(status: AlteredStatus):
    infix def withProbability(probability: Int): MoveEffect =
      AlteredState(status, accuracyFromPercent(probability))

  case class StatChangeEffectBuilder(stat: StatKind):
    infix def by(stages: Int): StatChangeProbabilityBuilder =
      StatChangeProbabilityBuilder(stat, stages)

  case class StatChangeProbabilityBuilder(stat: StatKind, stages: Int):
    infix def withProbability(probability: Int): MoveEffect =
      StatChange(stat, stages, accuracyFromPercent(probability))

