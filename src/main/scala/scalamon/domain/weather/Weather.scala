package scalamon.domain.weather

import scala.util.Random

/**
 * Enumerates the weather conditions that can affect a battle.
 *
 * Each weather value is later interpreted by the battle logic to determine
 * move modifiers, status interactions, and end-of-turn effects.
 */
enum Weather:

  /** Neutral weather with no special battle effects. */
  case ClearSky

  /** Strong sunlight that enhances some effects and suppresses others. */
  case HeavySunlight

  /** Rainfall that alters move interactions and type-specific weather effects. */
  case Rain

  /** Low-visibility weather that affects accuracy and some status chances. */
  case Fog

  /** Stormy weather with Electric-oriented offensive and status effects. */
  case Thunderstorm

object Weather:

  /**
   * Returns a random weather condition among all defined values.
   *
   * @return a randomly selected weather
   */
  def random: Weather =
    Weather.fromOrdinal(Random.nextInt(Weather.values.length))