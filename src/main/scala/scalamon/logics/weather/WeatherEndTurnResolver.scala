package scalamon.logics.weather

import scalamon.logics.log.BattleLogger
import scalamon.logics.state.BattleStateModuleImpl.{BattleState, PlayerState, switchSelfOpponent, updateLogs}
import scalamon.logics.state.PlayerStateModuleImpl.active
import scalamon.logics.state.PokemonStateModuleImpl.*

/**
 * Resolves end-of-turn effects produced by the active weather.
 *
 * The resolver updates both sides of the battle by applying weather-driven
 * residual damage and healing to their active Pokémon. If weather effects are
 * suppressed, the battle state is returned unchanged.
 */
trait WeatherEndTurnResolver:

  /**
   * Applies the active weather's end-of-turn effects to the battle state.
   *
   * The current weather is inspected once, then the resolver applies damage
   * and healing to each side's active Pokémon from their own perspective.
   *
   * @param state the current battle state
   * @return the updated battle state after weather resolution
   */
  def apply(state: BattleState): BattleState

object WeatherEndTurnResolver:

  /**
   * Default implementation of the weather end-of-turn resolver.
   *
   * It uses the contextual ⁠ WeatherSystem ⁠ to compute residual damage and
   * healing fractions for the active Pokémon on both sides.
   */
  given default(using weatherSystem: WeatherSystem): WeatherEndTurnResolver with

    /**
     * Applies weather effects unless the weather is currently suppressed.
     *
     * The self side is updated first, then the battle perspective is flipped
     * so the same logic can be reused for the opponent side.
     *
     * @param state the current battle state
     * @return the updated battle state
     */
    override def apply(state: BattleState): BattleState =
      if state.self.flags.weatherSuppressed then state
      else
        val afterSelf = applyAndLogCurrentSide(state)
        val fromOpponentView = switchSelfOpponent(afterSelf)
        val afterOpponentFromOpponentView = applyAndLogCurrentSide(fromOpponentView)
        switchSelfOpponent(afterOpponentFromOpponentView)

    /**
     * Applies weather damage/heal to the currently active Pokémon on the
     * side represented by ⁠ state.self ⁠, and logs the applied effects.
     *
     * @param state the battle state from the current side's perspective
     * @return the updated battle state for that side
     */
    private def applyAndLogCurrentSide(state: BattleState): BattleState =
      if isKnockedOut(state.self) then state
      else
        val activePokemon = state.self.getActive
        val weather = state.weather
        val pokemonType = activePokemon.species.pokemonType
        val maxHp = activePokemon.maxHp

        val damage =
          (maxHp * weatherSystem.endTurnDamageFraction(weather, pokemonType)).toInt

        val healAmount =
          (maxHp * weatherSystem.endTurnHealFraction(weather, pokemonType)).toInt

        val updatedState =
          state.copy(self = applyToActive(state.self, damage, healAmount))

        val withDamageLog =
          if damage > 0 then
            updateLogs(BattleLogger.logWeatherDamage(activePokemon, weather, damage))(updatedState)
          else updatedState

        if healAmount > 0 then
          updateLogs(BattleLogger.logWeatherHeal(activePokemon, weather, healAmount))(withDamageLog)
        else withDamageLog

    /**
     * Applies the resolved damage and healing to the active Pokémon of a player.
     *
     * @param player the player whose active Pokémon is being updated
     * @param damage the HP to remove
     * @param healAmount the HP to restore
     * @return the updated player state
     */
    private def applyToActive(player: PlayerState, damage: Int, healAmount: Int): PlayerState =
      val act = player.getActive
      val updatedActive = heal(healAmount)(takeDamage(damage)(act))
      active(_ => updatedActive)(player)

    private def isKnockedOut(player: PlayerState): Boolean =
      player.getActive.currentHp <= 0