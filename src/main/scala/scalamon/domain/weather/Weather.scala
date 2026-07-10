package scalamon.domain.weather

import scala.util.Random

enum Weather:
  case ClearSky
  case HeavySunlight
  case Rain
  case Fog
  case Thunderstorm

object Weather:
  def random: Weather = Weather.fromOrdinal(Random.nextInt(Weather.values.length))
