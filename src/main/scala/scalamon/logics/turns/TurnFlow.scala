package scalamon.logics.turns

import scalamon.logics.state.BattleStateImpl.{BattleState, PlayerState}

/**
 * Pair of actions selected by the two trainers for the current turn.
 *
 * @param player1Action
 *   the action selected by player 1
 * @param player2Action
 *   the action selected by player 2
 */
final case class TurnChoices(player1Action: BattleAction, player2Action: BattleAction)

/**
 * Execution order of the two actions chosen for a turn.
 */
enum ActionOrder:
  /**
   * Player 1 acts before player 2.
   */
  case Player1First

  /**
   * Player 2 acts before player 1.
   */
  case Player2First

/**
 * Computes the action execution order for a battle turn.
 *
 * The order is determined first by action priority and, in case of a tie,
 * by the speed of the two players' active Pokémon.
 */
trait TurnFlow:
  /**
   * Determines which player acts first in the current turn.
   *
   * Action priority takes precedence over speed. If both actions have the same
   * priority, the active Pokémon speeds are compared to resolve the ordering.
   *
   * @param state
   *   the current battle state
   * @param choices
   *   the actions selected for the turn
   * @param speedOf
   *   a function returning the speed associated with a player state
   * @return
   *   the action execution order for the turn
   */
  def actionOrdering(state: BattleState, choices: TurnChoices, speedOf: PlayerState => Speed): ActionOrder

object TurnFlow:

  /**
   * Determines the action execution order for the current turn.
   *
   * Player 1 acts first if its selected action has higher priority, or if both
   * actions have the same priority and player 1's active Pokémon is at least as
   * fast as the opponent's active Pokémon.
   *
   * @param state
   *   the current battle state
   * @param choices
   *   the actions selected for the turn
   * @param speedOf
   *   a function returning the speed associated with a player state
   * @return
   *   the resolved action execution order
   */
  def actionOrdering(state: BattleState, choices: TurnChoices, speedOf: PlayerState => Speed): ActionOrder =

    if choices.player1Action.priority > choices.player2Action.priority then
      ActionOrder.Player1First
    else if choices.player1Action.priority < choices.player2Action.priority then
      ActionOrder.Player2First
    else if speedOf(state.self) >= speedOf(state.opponent) then
      ActionOrder.Player1First
    else
      ActionOrder.Player2First