package scalamon.logics.weather

import org.scalatest.funsuite.AnyFunSuite
import scalamon.domain.moves.AlteredStatus.*
import scalamon.domain.types.Type.*
import scalamon.domain.weather.Weather.*
import scalamon.logics.weather.WeatherSystem.given

final class WeatherSystemTest extends AnyFunSuite:

  private val weatherSystem = summon[WeatherSystem]

  test("HeavySunlight boosts Fire moves") {
    assert(weatherSystem.movePowerMultiplier(HeavySunlight, Fire) == 1.7)
  }

  test("HeavySunlight weakens Water moves") {
    assert(weatherSystem.movePowerMultiplier(HeavySunlight, Water) == 0.7)
  }

  test("Rain boosts Water moves") {
    assert(weatherSystem.movePowerMultiplier(Rain, Water) == 1.3)
  }

  test("Rain weakens Fire moves") {
    assert(weatherSystem.movePowerMultiplier(Rain, Fire) == 0.7)
  }

  test("Thunderstorm boosts Electric moves") {
    assert(weatherSystem.movePowerMultiplier(Thunderstorm, Electric) == 1.3)
  }

  test("Thunderstorm weakens Grass moves") {
    assert(weatherSystem.movePowerMultiplier(Thunderstorm, Grass) == 0.7)
  }

  test("unrelated move-weather combinations keep neutral power multiplier") {
    assert(weatherSystem.movePowerMultiplier(Rain, Grass) == 1.0)
    assert(weatherSystem.movePowerMultiplier(ClearSky, Fire) == 1.0)
  }

  test("Fog lowers accuracy") {
    assert(weatherSystem.accuracyMultiplier(Fog) == 0.8)
  }

  test("non-fog weather keeps neutral accuracy") {
    assert(weatherSystem.accuracyMultiplier(ClearSky) == 1.0)
    assert(weatherSystem.accuracyMultiplier(Rain) == 1.0)
  }

  test("Rain makes Electric moves ignore accuracy") {
    assert(weatherSystem.ignoresAccuracy(Rain, Electric))
  }

  test("other move-weather combinations do not ignore accuracy") {
    assert(!weatherSystem.ignoresAccuracy(Rain, Fire))
    assert(!weatherSystem.ignoresAccuracy(Fog, Electric))
  }

  test("HeavySunlight blocks freeze") {
    assert(weatherSystem.blocksFreeze(HeavySunlight))
  }

  test("weather different from HeavySunlight does not block freeze") {
    assert(!weatherSystem.blocksFreeze(Fog))
    assert(!weatherSystem.blocksFreeze(ClearSky))
  }

  test("Fog increases Psychic sleep chance") {
    assert(weatherSystem.sleepChanceMultiplier(Fog, Psychic) == 1.1)
  }

  test("other move-weather combinations keep neutral sleep chance") {
    assert(weatherSystem.sleepChanceMultiplier(Fog, Fire) == 1.0)
    assert(weatherSystem.sleepChanceMultiplier(Rain, Psychic) == 1.0)
  }

  test("Thunderstorm overrides Electric paralysis chance") {
    assert(weatherSystem.paralysisChanceOverride(Thunderstorm, Electric).contains(0.7))
  }

  test("other move-weather combinations keep default paralysis logic") {
    assert(weatherSystem.paralysisChanceOverride(Rain, Electric).isEmpty)
    assert(weatherSystem.paralysisChanceOverride(Thunderstorm, Fire).isEmpty)
  }

  test("HeavySunlight increases Poison residual damage") {
    assert(weatherSystem.residualDamageMultiplier(HeavySunlight, Poisoned) == 1.2)
  }

  test("other move-weather combinations keep neutral residual damage") {
    assert(weatherSystem.residualDamageMultiplier(HeavySunlight, Burned) == 1.0)
    assert(weatherSystem.residualDamageMultiplier(ClearSky, Poisoned) == 1.0)
  }

  test("Rain damages Fire types at end of turn") {
    assert(weatherSystem.endTurnDamageFraction(Rain, Fire) == 1.0 / 16.0)
  }

  test("Thunderstorm damages non-Electric types at end of turn") {
    assert(weatherSystem.endTurnDamageFraction(Thunderstorm, Fire) == 1.0 / 16.0)
  }

  test("Thunderstorm does not damage Electric types at end of turn") {
    assert(weatherSystem.endTurnDamageFraction(Thunderstorm, Electric) == 0.0)
  }

  test("Fog heals Psychic types at end of turn") {
    assert(weatherSystem.endTurnHealFraction(Fog, Psychic) == 1.0 / 16.0)
  }

  test("other move-weather combinations have no end-of-turn healing") {
    assert(weatherSystem.endTurnHealFraction(Fog, Fire) == 0.0)
    assert(weatherSystem.endTurnHealFraction(ClearSky, Psychic) == 0.0)
  }

  test("ClearSky keeps neutral behaviour") {
    assert(weatherSystem.movePowerMultiplier(ClearSky, Fire) == 1.0)
    assert(weatherSystem.accuracyMultiplier(ClearSky) == 1.0)
    assert(!weatherSystem.ignoresAccuracy(ClearSky, Electric))
    assert(!weatherSystem.blocksFreeze(ClearSky))
    assert(weatherSystem.sleepChanceMultiplier(ClearSky, Psychic) == 1.0)
    assert(weatherSystem.paralysisChanceOverride(ClearSky, Electric).isEmpty)
    assert(weatherSystem.residualDamageMultiplier(ClearSky, Poisoned) == 1.0)
    assert(weatherSystem.endTurnDamageFraction(ClearSky, Fire) == 0.0)
    assert(weatherSystem.endTurnHealFraction(ClearSky, Psychic) == 0.0)
  }