package domain

import services.Action

trait Action:
  def execute(state: GameState): GameState
  def priority(): Int

case class MoveAction(move: Move) extends Action
  
  