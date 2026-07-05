package scalamon.logics.turns

import scalamon.logics.state.BattleStateImpl.{BattleState, PlayerState}
import scalamon.logics.turns.BattleAction
import scalamon.logics.turns.UseMove
import scalamon.logics.turns.SwitchPokemon

/**
 * The two actions selected by the trainers for the current turn.
 *
 * @param first
 *   the first chosen action
 * @param second
 *   the second chosen action
 */
final case class TurnChoices(first: BattleAction, second: BattleAction)

enum ActionOrder:
  case SelfFirst, OpponentFirst


/**
 * Resolves the execution plan for a turn starting from the players' choices.
 */
trait TurnFlow:
  /**
   * Starts a turn by scheduling the chosen actions and resolving their execution order.
   *
   * @param choices
   * the actions selected for the turn
   * @param speedOf
   * a function returning the speed of a Pokémon reference
   * @return
   * the execution plan for the turn
   */
  def actionOrdering(state: BattleState,choices: TurnChoices , speedOf: PlayerState => Speed): ActionOrder

object TurnFlow:

  def actionOrdering(state: BattleState, choices: TurnChoices, speedOf: PlayerState => Speed): ActionOrder =

    if choices.first.priority < choices.second.priority then
      ActionOrder.SelfFirst
    else if choices.first.priority > choices.second.priority then
      ActionOrder.OpponentFirst
    else if speedOf(state.self) >= speedOf(state.opponent) then
      ActionOrder.SelfFirst
    else
      ActionOrder.OpponentFirst
