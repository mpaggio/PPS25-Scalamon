package scalamon.logics.weather

import scalamon.domain.alteredStatus.AlteredStatus
import AlteredStatus.*
import scalamon.domain.types.Type
import scalamon.domain.weather.Weather
import scalamon.domain.weather.Weather.*
import scalamon.domain.types.Type.*

/**
 * Describes the battle weather rules used by the combat engine.
 *
 * Each method returns a pure modifier or a pure decision derived from the
 * current weather and the involved type. The implementation is intentionally
 * stateless so it can be safely used as a contextual service.
 *
 * The project currently supports the following weather conditions:
 * - ClearSky: no special effects
 * - HeavySunlight: boosts Fire, weakens Water, blocks freeze, and enhances poison residual damage
 * - Rain: boosts Water, weakens Fire, makes Electric moves ignore accuracy, and damages Fire types at end of turn
 * - Fog: lowers accuracy globally, increases Psychic sleep chance, and heals Psychic types at end of turn
 * - Thunderstorm: boosts Electric, weakens Grass, changes Electric paralysis odds, and damages non-Electric types at end of turn
 */
trait WeatherSystem:
  /**
   * Returns the power multiplier to apply to a move under the current weather.
   *
   * @param weather
   * the active weather condition
   * @param moveType
   * the type of the move being evaluated
   * @return
   * the power multiplier to apply
   */
  def movePowerMultiplier(weather: Weather, moveType: Type): Double

  /**
   * Returns the global accuracy multiplier imposed by the current weather.
   *
   * @param weather
   * the active weather condition
   * @return
   * the accuracy multiplier to apply to the move's base accuracy
   */
  def accuracyMultiplier(weather: Weather): Double

  /**
   * Indicates whether the current weather causes a move to bypass the accuracy check.
   *
   * @param weather
   * the active weather condition
   * @param moveType
   * the type of the move being evaluated
   * @return
   * true if the move should ignore accuracy, false otherwise
   */
  def ignoresAccuracy(weather: Weather, moveType: Type): Boolean

  /**
   * Indicates whether the current weather prevents freeze from being applied.
   *
   * @param weather
   * the active weather condition
   * @return
   * true if freeze is blocked, false otherwise
   */
  def blocksFreeze(weather: Weather): Boolean

  /**
   * Returns the multiplier to apply to a move's chance of causing sleep.
   *
   * @param weather
   * the active weather condition
   * @param moveType
   * the type of the move being evaluated
   * @return
   * the multiplier to apply to the sleep chance
   */
  def sleepChanceMultiplier(weather: Weather, moveType: Type): Double

  /**
   * Returns a weather-specific override for paralysis chance, if present.
   *
   * @param weather
   * the active weather condition
   * @param moveType
   * the type of the move being evaluated
   * @return
   * Some(chance) when the weather replaces the default paralysis chance,
   * None when the default paralysis logic should be kept
   */
  def paralysisChanceOverride(weather: Weather, moveType: Type): Option[Double]

  /**
   * Returns the multiplier to apply to residual damage effects associated with a move type.
   *
   * @param weather
   * the active weather condition
   * @param status
   * the type of the altered status being evaluated
   * @return
   * the residual damage multiplier to apply
   */
  def residualDamageMultiplier(weather: Weather, status: AlteredStatus): Double

  /**
   * Returns the fraction of max HP dealt as end-of-turn damage.
   *
   * @param weather
   * the active weather condition
   * @param pokemonType
   * the Pokémon's type
   * @return
   * the end-of-turn damage fraction, or 0.0 if the weather has no damage effect
   */
  def endTurnDamageFraction(weather: Weather, pokemonType: Type): Double

  /**
   * Returns the fraction of max HP restored as end-of-turn healing.
   *
   * @param weather
   * the active weather condition
   * @param pokemonType
   * the Pokémon's type
   * @return
   * the end-of-turn healing fraction, or 0.0 if the weather has no healing effect
   */
  def endTurnHealFraction(weather: Weather, pokemonType: Type): Double

object WeatherSystem:
  /**
   * Default weather rule set for the current battle system.
   */
  given default: WeatherSystem with
    override def movePowerMultiplier(weather: Weather, moveType: Type): Double = (weather, moveType) match
      case (HeavySunlight, Fire) => 1.7
      case (HeavySunlight, Water) => 0.7
      case (Rain, Water) => 1.3
      case (Rain, Fire) => 0.7
      case (Thunderstorm, Electric) => 1.3
      case (Thunderstorm, Grass) => 0.7
      case _ => 1.0

    override def accuracyMultiplier(weather: Weather): Double = weather match
      case Fog => 0.8
      case _ => 1.0

    override def ignoresAccuracy(weather: Weather, moveType: Type): Boolean = (weather, moveType) match
      case (Rain, Electric) => true
      case _ => false

    override def blocksFreeze(weather: Weather): Boolean = weather == HeavySunlight

    override def sleepChanceMultiplier(weather: Weather, moveType: Type): Double = (weather, moveType) match
      case (Fog, Psychic) => 1.1
      case _ => 1.0

    override def paralysisChanceOverride(weather: Weather, moveType: Type): Option[Double] = (weather, moveType) match
      case (Thunderstorm, Electric) => Some(0.7)
      case _ => None

    override def residualDamageMultiplier(weather: Weather, status: AlteredStatus): Double = (weather, status) match
      case (HeavySunlight, Poisoned) => 1.5
      case _ => 1.0

    override def endTurnDamageFraction(weather: Weather, pokemonType: Type): Double = (weather, pokemonType) match
      case (Rain, Fire) => 1.0 / 16.0
      case (Weather.Thunderstorm, tpe) if tpe != Electric => 1.0 / 16.0
      case _ => 0.0

    override def endTurnHealFraction(weather: Weather, pokemonType: Type): Double = (weather, pokemonType) match
      case (Fog, Psychic) => 1.0 / 16.0
      case _ => 0.0
