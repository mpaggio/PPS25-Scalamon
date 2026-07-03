package scalamon.domain.moves

import Accuracy.*
import Accuracy.ProbabilityRoll
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.state.DamageMoveCalculatorImpl.{Damage, getDamage}
import scalamon.logics.state.DamagePolicy

import scalamon.domain.actions.Action

case class MoveAction(move: Move, target: Target = Opponent)(using policy: DamagePolicy, roll: ProbabilityRoll) extends Action:
  override def apply(bs: BattleState): BattleState =
    val isHit = move.accuracy.test

    val ppStep: StateTransformer = self(active(updateMove(move.name)(decreasePpBy(1))))

    val damageStep: StateTransformer = battleState =>
      if isHit then move match
        case damageMove: DamageMove =>
          val damageActive = active(currentHp(decrease(getDamage(battleState, damageMove))))
          target match
            case Self => self(damageActive)(battleState)
            case Opponent => opponent(damageActive)(battleState)
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

    List(ppStep, damageStep, effectStep).foldLeft(bs)((state, transformer) => transformer(state))
