package scalamon.logics.weather

import scalamon.domain.weather.Weather
import scalamon.logics.state.BattleStateImpl.{BattleState, PlayerState}
import scalamon.logics.weather.WeatherEndTurnResolver
import scalamon.logics.weather.WeatherSystem

/**
 * Resolves weather-based end-of-turn effects on the current battle context.
 *
 * This component applies residual weather damage and weather healing to the
 * currently active Pokémon on both sides of the battle.
 */
trait WeatherEndTurnResolver:
  /**
   * Applies end-of-turn weather effects to the given battle context.
   *
   * @param context
   * the current battle context
   * @return
   * an updated battle context after applying weather damage and healing
   */
  def apply(state: BattleState): BattleState

object WeatherEndTurnResolver:
  /**
   * Default weather end-of-turn resolver.
   *
   * It uses the contextual `WeatherSystem` to determine whether the active
   * Pokémon on each side should take damage or receive healing at the end
   * of the turn.
   */
  given default(using weatherSystem: WeatherSystem): WeatherEndTurnResolver with

    override def apply(state: BattleState): BattleState =
      if state.flags.weatherSuppressed then state
      else
        val weather = state.weather
        state.copy(
          self = applyToActive(state.self, weather),
          opponent = applyToActive(state.opponent, weather)
        )

    /**
     * Applies weather end-of-turn effects to the active Pokémon of a player.
     *
     * @param player
     * the player whose active Pokémon is being updated
     * @param weather
     * the active weather condition
     * @return
     * the updated player state
     */
    private def applyToActive(player: PlayerState, weather: Weather): PlayerState =
      val active = player.getActive
      val pokemonType = active.species.pokemonType
      val damageFraction = weatherSystem.endTurnDamageFraction(weather, pokemonType)
      val healFraction = weatherSystem.endTurnHealFraction(weather, pokemonType)
      val maxHp = active.species.baseStats.hp.toInt
      val damage = (maxHp * damageFraction).toInt
      val heal = (maxHp * healFraction).toInt
      val updateActive = active.takeDamage(damage).heal(heal)
      player.active(_ => updateActive)