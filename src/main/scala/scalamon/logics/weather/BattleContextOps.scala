package scalamon.logics.weather

import scalamon.domain.weather.Weather
import scalamon.logics.battle.{BattleContext, WeatherState}

object BattleContextOps:
  extension (battleContext: BattleContext)
    def currentWeather: Weather = battleContext.weatherState.current

    def withWeather(weather: Weather): BattleContext = battleContext.copy(weatherState = WeatherState(weather))