package scalamon.domain.moves

import Accuracy.*
import Accuracy.ProbabilityRoll
import scalamon.domain.pokemon.abilities.Target
import scalamon.domain.pokemon.abilities.Target.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.state.DamageMoveCalculatorImpl.getDamage
import scalamon.logics.state.DamagePolicy

import scalamon.domain.actions.Action

/**
 * A concrete [[Action]] representing the execution logic of a Pokémon move during battle.
 *
 * This class orchestrates the complex sequence of events triggered by a move, including
 * resource consumption, accuracy testing, damage application and secondary effects.
 * It follows the StateTransformer pattern, treating the entire battle execution as a
 * pure transformation from one [[BattleState]] to the next.
 *
 * @param move The static domain data of the move being executed.
 * @param target The intended target of the move (defaults to Opponent).
 * @param policy The contextual [[DamagePolicy]] used to determine damage intensity.
 * @param roll The contextual [[ProbabilityRoll]] used for accuracy and effect checks.
 */
case class MoveAction(move: Move, target: Target = Opponent)(using policy: DamagePolicy, roll: ProbabilityRoll) extends Action:
  /**
   * Transforms the current battle state by executing the move's logic.
   *
   * The execution follows a 3-step pipeline:
   * 1. PP step: decreases the move's power points on the user's state.
   * 2. Damage step: if the move hits and is a [[DamageMove]], calculates and inflicts damage.
   * 3. Effect step: if the move hits, applies any secondary [[MoveEffect]] to the target.
   *
   * @param bs The initial [[BattleState]] before the move is executed.
   * @return A new [[BattleState]] reflecting all changes.
   */
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
