package scalamon.logics.weather

import scalamon.logics.battle.{BattleContext, WeatherState}
import scalamon.logics.state.BattleStateImpl.BattleState

/**
 * Helper utilities to convert between battle state and weather-aware battle context.
 *
 * This adapter keeps the weather model separate from the core battle state while
 * providing a minimal bridge for weather-aware logic.
 */
object WeatherBattleAdapter:
  /**
   * Builds a battle context from a battle state and the current weather.
   *
   * @param state
   * the current battle state
   * @param weather
   * the current weather state
   * @return
   * a battle context carrying both state and weather information
   */
  def from(state: BattleState, weatherState: WeatherState): BattleContext = BattleContext(state, weatherState)

  /**
   * Extracts the battle state from a battle context.
   *
   * @param context
   * the weather-aware battle context
   * @return
   * the underlying battle state
   */
  def toState(context: BattleContext): BattleState = context.state