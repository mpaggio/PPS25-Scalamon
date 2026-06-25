package scalamon.logics.battle

import scalamon.domain.weather.Weather
import scalamon.logics.state.BattleStateImpl.BattleState

/**
 * Represents the runtime weather state of an ongoing battle.
 *
 * @param current
 *   the currently active weather condition
 */
final case class WeatherState(current: Weather)

/**
 * Aggregates the dynamic battle state together with the current weather state.
 *
 * This wrapper is useful when battle logic needs access to both the core
 * battle state and contextual environmental information, such as weather.
 *
 * @param state
 *   the current battle state
 * @param weatherState
 *   the current weather state associated with the battle
 */
final case class BattleContext(state: BattleState, weatherState: WeatherState)