package scalamon.logics.weather

import org.scalatest.funsuite.AnyFunSuite
import scalamon.domain.types.Type.*
import scalamon.domain.weather.Weather.*

final class WeatherSystemTest extends AnyFunSuite:

  private val weatherSystem = summon[WeatherSystem]

  test("HeavySunLight boosts fire moves") {
    assert(weatherSystem.movePowerMultiplier(HeavySunlight, Fire) == 1.7)
  }

  test("Fog lowers accuracy") {
    assert(weatherSystem.accuracyMultiplier(Fog) == 0.8)
  }

  test("Rain makes Electric moves ignore accuracy") {
    assert(weatherSystem.ignoresAccuracy(Rain, Electric))
  }

  test("HeavySunlight blocks freeze"){
    assert(weatherSystem.blocksFreeze(HeavySunlight))
  }

  test("Fog increases Psychic sleep chance") {
    assert(weatherSystem.sleepChanceMultiplier(Fog, Psychic) == 1.1)
  }

  test("Rain damages Fire types at end of turn") {
    assert(weatherSystem.endTurnDamageFraction(Rain, Fire) == 1.0 / 16.0)
  }

  test("Fog heals Psychic types at end of turn") {
    assert(weatherSystem.endTurnHealFraction(Fog, Psychic) == 1.0 / 16.0)
  }

  test("Clear weather has neutral multipliers") {
    assert(weatherSystem.movePowerMultiplier(ClearSky, Fire) == 1.0)
    assert(weatherSystem.accuracyMultiplier(ClearSky) == 1.0)
    assert(weatherSystem.endTurnDamageFraction(ClearSky, Fire) == 0.0)
    assert(weatherSystem.endTurnHealFraction(ClearSky, Psychic) == 0.0)
  }