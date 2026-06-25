package scalamon.logics.weather

import org.scalatest.funsuite.AnyFunSuite
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.pokemon.abilities.AbilitySlot
import scalamon.domain.pokemon.pokedex.PokedexADT
import scalamon.domain.pokemon.pokedex.PokedexADT.PokedexId
import scalamon.domain.pokemon.statistics.StatADT.fromInt
import scalamon.domain.pokemon.statistics.Stats
import scalamon.domain.types.Type.*
import scalamon.domain.weather.Weather.*
import scalamon.logics.battle.{BattleContext, WeatherState}
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.MoveStateModuleImpl.*
import scalamon.domain.pokemon.abilities.Ability.*

class WeatherEndTurnResolverTest extends AnyFunSuite:

  private def mkStats(hp: Int): Stats =
    Stats(
      hp = fromInt(hp),
      attack = fromInt(50),
      defense = fromInt(50),
      specialAttack = fromInt(50),
      specialDefense = fromInt(50),
      speed = fromInt(50)
    )

  private def mkPokemon(name: String, tpe: scalamon.domain.types.Type, hp: Int): Pokemon =
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

  private def mkContext(selfType: scalamon.domain.types.Type, selfHp: Int, weather: scalamon.domain.weather.Weather): BattleContext =
    val selfPokemon = mkPokemon("self", selfType, 160)
    val oppPokemon = mkPokemon("opp", Water, 160)
    val selfState = mkPokemonState(selfPokemon, selfHp)
    val oppState = mkPokemonState(oppPokemon, 160)
    val selfPlayer = playerState(Map("self" -> selfState), "self")
    val oppPlayer = playerState(Map("opp" -> oppState), "opp")
    BattleContext(
      state = battleState(selfPlayer, oppPlayer),
      weatherState = WeatherState(weather)
    )

  test("Rain damages Fire active pokemon at the end of the turn") {
    val context = mkContext(Fire, 160, Rain)
    val updated = summon[WeatherEndTurnResolver].apply(context)
    assert(updated.state.self.getActive.currentHp == 150)
  }

  test("Fog heals Psychic type at the end of the turn") {
    val context = mkContext(Psychic, 80, Fog)
    val updated = summon[WeatherEndTurnResolver].apply(context)
    assert(updated.state.self.getActive.currentHp == 90)
  }

  test("ClearSky does not change hp") {
    val context = mkContext(Fire, 120, ClearSky)
    val updated = summon[WeatherEndTurnResolver].apply(context)
    assert(updated.state.self.getActive.currentHp == 120)
  }
