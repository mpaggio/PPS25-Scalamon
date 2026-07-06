package scalamon.logics.turns

import scalamon.logics.state.BattleStateImpl.{BattleState, PlayerState}
import scalamon.logics.turns.BattleAction
import scalamon.logics.turns.UseMove
import scalamon.logics.turns.SwitchPokemon

/**
 * The two actions selected by the trainers for the current turn.
 *
 * @param player1Action
 *   the first chosen action
 * @param player2Action
 *   the second chosen action
 */
final case class TurnChoices(player1Action: BattleAction, player2Action: BattleAction)

enum ActionOrder:
  case Player1First, Player2First


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

    if choices.player1Action.priority < choices.player2Action.priority then
      ActionOrder.Player1First
    else if choices.player1Action.priority > choices.player2Action.priority then
      ActionOrder.Player2First
    else if speedOf(state.self) >= speedOf(state.opponent) then
      ActionOrder.Player1First
    else
      ActionOrder.Player2First
