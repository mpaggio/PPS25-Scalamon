package scalamon.domain.moves

import Accuracy.*
import Accuracy.ProbabilityRoll
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.state.DamageMoveCalculatorImpl.{Damage, getDamage}
import scalamon.logics.state.DamagePolicy
import scalamon.domain.moves.EffectTarget.*

trait MoveActionModule:
  type Action

object MoveActionModuleImpl extends MoveActionModule:
  override type Action = List[StateTransformer]

  case class MoveAction(move: Move)(using policy: DamagePolicy):
    def execute(target: EffectTarget = Opponent)(using roll: ProbabilityRoll): Action =
      val isHit = move.accuracy.test
      
      val ppStep: StateTransformer = self(active(updateMove(move.name)(decreasePpBy(1))))
          
      val damageStep: StateTransformer = battleState =>
        if isHit then move match
          case damageMove: DamageMove => 
            val damageAmount: Damage = getDamage(battleState, damageMove)
            target match
              case Self => self(active(currentHp(decrease(damageAmount))))(battleState)
              case Opponent => opponent(active(currentHp(decrease(damageAmount))))(battleState)
          case statusMove: StatusMove => battleState
        else
          battleState
          
      val effectStep: StateTransformer = battleState =>
        target match
          case Opponent =>
            if isHit then move match
              case damageMove: DamageMove => damageMove.effect match
                case Some(effect: MoveEffect) => effect.executeEffect(battleState)
                case None => battleState
              case statusMove: StatusMove => statusMove.effect.executeEffect(battleState)
            else battleState
          case Self => battleState
        
      List(ppStep, damageStep, effectStep)