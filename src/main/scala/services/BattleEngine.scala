package services

import services.State.GameState

trait BattleEngine {}
  
  
object BattleEngine(gameState: GameState, playerActions: (PlayerAction, PlayerAction)):
  
  
  def apply(): GameState = alterateStatus playerActions._1.execute andThen playerActions._2.execute apply gameState 


