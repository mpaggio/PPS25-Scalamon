package scalamon.domain.moves

import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.state.StatsStateModuleImpl.*
import scalamon.logics.state.DamageMoveCalculatorImpl.{getDamage, Damage}
import scalamon.logics.state.DamagePolicy

trait MoveActionModule:
  type Action

object MoveActionModuleImpl extends MoveActionModule:
  override type Action = List[StateTransformer]

  case class MoveAction(move: Move)(using policy: DamagePolicy):
    def apply(battleState: BattleState): Action = List(
      move match
        case damageMove: DamageMove =>
          battleState =>
            val damageAmount: Damage = getDamage(battleState, damageMove)
            battleState opponent (_ active (_ currentHp (_ decrease damageAmount)))
        case statusMove: StatusMove =>
          identity[BattleState]
    )