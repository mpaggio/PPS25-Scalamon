package scalamon.logics.turns

import scalamon.logics.state.BattleStateImpl.{BattleState, battleState}
import scalamon.logics.state.PlayerStateModuleImpl.PlayerState
import scalamon.logics.teambuilder.TeamBuilder.TeamBuilder

/**
 * Companion object for BattleSetup.
 * Provides a namespace for the battle setup logic.
 */
object BattleSetup:

  /**
   * Sets up a battle between two players by building their teams using the provided [[TeamBuilder]] instances.
   * @param player1Builder The [[TeamBuilder]] instance for player 1, responsible for constructing their team.
   * @param player2Builder The [[TeamBuilder]] instance for player 2, responsible for constructing their team.
   * @return
   */
  def setupBattle(player1Builder: TeamBuilder, player2Builder: TeamBuilder): BattleState =
    val player1: PlayerState = player1Builder.buildTeam()
    val player2: PlayerState = player2Builder.buildTeam()
    battleState(player1, player2)