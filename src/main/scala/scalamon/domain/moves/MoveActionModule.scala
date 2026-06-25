package scalamon.domain.moves

import Accuracy.*
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.state.StatsStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.MoveStateModuleImpl.*
import scalamon.logics.state.DamageMoveCalculatorImpl.{Damage, getDamage}
import scalamon.logics.state.DamagePolicy

import scala.util.Random

trait MoveActionModule:
  type Action

object MoveActionModuleImpl extends MoveActionModule:
  type ProbabilityRoll = () => Int
  given defaultRoll: ProbabilityRoll = () => Random.nextInt(100) + 1
  override type Action = List[StateTransformer]

  case class MoveAction(move: Move)(using policy: DamagePolicy, roll: ProbabilityRoll):
    def execute: Action =
      val isHit = checkAccuracy(move.accuracy)(using roll)
      
      val ppStep: StateTransformer = battleState =>
        battleState self (_ active (_ .updateMove(move.name)(_.decreasePpBy(1))))
          
      val damageStep: StateTransformer = battleState =>
        if isHit then move match
          case damageMove: DamageMove => 
            val damageAmount: Damage = getDamage(battleState, damageMove)
            battleState opponent (_ active (_ currentHp (_ decrease damageAmount)))
          case statusMove: StatusMove => battleState
        else
          battleState
          
      val effectStep: StateTransformer = battleState =>
        if isHit then move match
          case damageMove: DamageMove => damageMove.effect match
            case Some(effect: MoveEffect) => effect.executeEffect(battleState)
            case None => battleState
          case statusMove: StatusMove => statusMove.effect.executeEffect(battleState)
        else battleState
        
      List(ppStep, damageStep, effectStep)

    private def checkAccuracy(accuracy: Accuracy)(using roll: ProbabilityRoll): Boolean =
      roll() <= accuracy.asInt