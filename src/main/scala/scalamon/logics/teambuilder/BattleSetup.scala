package scalamon.logics.teambuilder

import scalamon.domain.weather.Weather
import scalamon.logics.state.BattleStateModuleImpl.{BattleState, battleState}
import scalamon.logics.state.PlayerStateModuleImpl.PlayerState
import scalamon.logics.teambuilder.TeamBuilder.TeamBuilder

/**
 * Companion object for BattleSetup.
 * Provides a namespace for the battle setup logic.
 */
object BattleSetup:

  /**
   * Sets up a battle between two players, each with their own team builder and name.
   *
   * @param player1 a tuple containing the first player's team builder and name
   * @param player2 a tuple containing the second player's team builder and name
   * @return the initial state of the battle
   */
  def setupBattle(player1: (TeamBuilder, String), player2: (TeamBuilder, String)): BattleState =
    val player1State: PlayerState = player1._1.buildTeam(player1._2)
    val player2State: PlayerState = player2._1.buildTeam(player2._2)
    battleState(player1State, player2State, Weather.random)