package scalamon.logics.weather

import org.scalatest.funsuite.AnyFunSuite
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.pokemon.abilities.Ability.*
import scalamon.domain.pokemon.abilities.AbilitySlot
import scalamon.domain.pokemon.pokedex.PokedexADT
import scalamon.domain.pokemon.statistics.StatADT.fromInt
import scalamon.domain.pokemon.statistics.Stats
import scalamon.domain.types.Type
import scalamon.domain.types.Type.*
import scalamon.domain.weather.Weather
import scalamon.domain.weather.Weather.*
import scalamon.logics.battle.{BattleContext, WeatherState}
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.weather.WeatherEndTurnResolver.given
import scalamon.logics.weather.WeatherSystem.given

final class WeatherEndTurnResolverTest extends AnyFunSuite:

  private def mkStats(hp: Int): Stats =
    Stats(
      hp = fromInt(hp),
      attack = fromInt(50),
      defense = fromInt(50),
      specialAttack = fromInt(50),
      specialDefense = fromInt(50),
      speed = fromInt(50)
    )

  private def mkPokemon(name: String, tpe: Type, hp: Int): Pokemon =
    Pokemon(
      pokedexId = PokedexADT.fromInt(1),
      name = name,
      pokemonType = tpe,
      baseStats = mkStats(hp),
      abilitySlot = AbilitySlot(primary = Blaze)
    )

  private def mkPokemonState(pokemon: Pokemon, currentHp: Int): PokemonState =
    pokemonInitialState(pokemon, Map.empty)
      .currentHp(_ => currentHp)

  private def mkContext(
                         selfType: Type,
                         selfHp: Int,
                         opponentType: Type = Water,
                         opponentHp: Int = 160,
                         weather: Weather = ClearSky
                       ): BattleContext =
    val selfPokemon = mkPokemon("self", selfType, 160)
    val opponentPokemon = mkPokemon("opp", opponentType, 160)
    val selfState = mkPokemonState(selfPokemon, selfHp)
    val opponentState = mkPokemonState(opponentPokemon, opponentHp)
    val selfPlayer = playerState(Map("self" -> selfState), "self")
    val opponentPlayer = playerState(Map("opp" -> opponentState), "opp")
    BattleContext(
      state = battleState(selfPlayer, opponentPlayer),
      weatherState = WeatherState(weather)
    )

  test("Rain damages Fire active pokemon at the end of the turn") {
    val context = mkContext(selfType = Fire, selfHp = 160, weather = Rain)
    val updated = summon[WeatherEndTurnResolver].apply(context)
    assert(updated.state.self.getActive.currentHp == 150)
  }

  test("Fog heals Psychic active pokemon at the end of the turn") {
    val context = mkContext(selfType = Psychic, selfHp = 80, weather = Fog)
    val updated = summon[WeatherEndTurnResolver].apply(context)
    assert(updated.state.self.getActive.currentHp == 90)
  }

  test("ClearSky does not change hp") {
    val context = mkContext(selfType = Fire, selfHp = 120, weather = ClearSky)
    val updated = summon[WeatherEndTurnResolver].apply(context)
    assert(updated.state.self.getActive.currentHp == 120)
    assert(updated.state.opponent.getActive.currentHp == 160)
  }

  test("Thunderstorm damages non-Electric active pokemon on both sides") {
    val context = mkContext(
      selfType = Fire,
      selfHp = 160,
      opponentType = Grass,
      opponentHp = 160,
      weather = Thunderstorm
    )
    val updated = summon[WeatherEndTurnResolver].apply(context)
    assert(updated.state.self.getActive.currentHp == 150)
    assert(updated.state.opponent.getActive.currentHp == 150)
  }

  test("Thunderstorm does not damage Electric active pokemon") {
    val context = mkContext(
      selfType = Electric,
      selfHp = 160,
      opponentType = Electric,
      opponentHp = 160,
      weather = Thunderstorm
    )
    val updated = summon[WeatherEndTurnResolver].apply(context)
    assert(updated.state.self.getActive.currentHp == 160)
    assert(updated.state.opponent.getActive.currentHp == 160)
  }

  test("Fog healing does not exceed max hp") {
    val context = mkContext(selfType = Psychic, selfHp = 155, weather = Fog)
    val updated = summon[WeatherEndTurnResolver].apply(context)
    assert(updated.state.self.getActive.currentHp == 160)
  }

  test("weatherSuppressed prevents weather end-turn effects") {
    val context =
      mkContext(selfType = Fire, selfHp = 160, weather = Rain).copy(
        state = mkContext(selfType = Fire, selfHp = 160, weather = Rain).state
          .updateFlags(_.copy(weatherSuppressed = true))
      )
    val updated = summon[WeatherEndTurnResolver].apply(context)
    assert(updated.state.self.getActive.currentHp == 160)
    assert(updated.state.opponent.getActive.currentHp == 160)
  }