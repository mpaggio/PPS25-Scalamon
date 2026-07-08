package scalamon.gui

import scalamon.logics.state.BattleStateImpl.BattleState

/**
 * This object provides utility functions to adapt the BattleState for use in a State monad context.
 */
object BattleStateAdapter:

  /**
   * Converts a function that transforms a BattleState into a State monad action.
   * @param op A function that takes a BattleState and returns a new BattleState.
   * @return A State monad action that applies the transformation to the current BattleState.
   */
  def fromOp(op: BattleState => BattleState): State[BattleState, Unit] =
    State(s => (op(s), ()))

  /**
   * Retrieves the current BattleState as a string representation.  
   * @return A State monad action that returns the string representation of the current BattleState.
   */
  def getStatus: State[BattleState, String] =
    State(s => (s, s.toString)) 