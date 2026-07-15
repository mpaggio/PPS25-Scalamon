package scalamon.domain.moves

import Accuracy.*
import scalamon.domain.moves.MoveEffect.*
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
    def execute: Action = List(
      (battleState: BattleState) =>
        val activePokemon = battleState.self.team(battleState.self.activeId)
        val currentMoveState = activePokemon.moveState(move.name)

        if currentMoveState.currentPp <= 0
          then battleState
        else
          val stateAfterPp: BattleState =
            battleState self (_ active (_.updateMove(move.name)(_.decreasePpBy(1))))
          if checkAccuracy(move.accuracy) then move match
            case damageMove: DamageMove =>
              val damageAmount: Damage = getDamage(stateAfterPp, damageMove)
              val stateAfterDamage: BattleState =
                stateAfterPp opponent (_ active (_ currentHp (_ decrease damageAmount)))
              damageMove.effect match
                case Some(recoil: Recoil) =>
                  val recoilAmount = recoil.percentage * activePokemon.species.baseStats.hp.toInt / 100
                  stateAfterDamage self (_ active (_ takeDamage recoilAmount))
                case _ => stateAfterDamage
            case statusMove: StatusMove => statusMove.effect match
              case healing: Heal =>
                val healAmount = healing.percentage * activePokemon.species.baseStats.hp.toInt / 100
                stateAfterPp self (_ active (_ heal (healAmount)))
              case _ => stateAfterPp
          else
            stateAfterPp
    )

    private def checkAccuracy(accuracy: Accuracy)(using roll: ProbabilityRoll): Boolean =
      roll() <= accuracy.asInt