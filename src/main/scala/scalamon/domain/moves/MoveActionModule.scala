package scalamon.domain.moves

import Accuracy.*
import Accuracy.ProbabilityRoll
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.state.StatsStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.MoveStateModuleImpl.*
import scalamon.logics.state.DamageMoveCalculatorImpl.{Damage, getDamage}
import scalamon.logics.state.DamagePolicy

trait MoveActionModule:
  type Action

object MoveActionModuleImpl extends MoveActionModule:
  override type Action = List[StateTransformer]

  case class MoveAction(move: Move)(using policy: DamagePolicy):
    def execute(using roll: ProbabilityRoll): Action =
      val isHit = move.accuracy.test
      
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