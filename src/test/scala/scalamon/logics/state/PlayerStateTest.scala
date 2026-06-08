package scalamon.logics.state

import org.scalatest.funsuite.AnyFunSuite
import scalamon.domain.pokemon.pokedex.MyPokedex
import scalamon.domain.pokemon.statistics.StatADT.fromInt
import scalamon.domain.pokemon.statistics.Stats
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.StatsStateModuleImpl.statsInitialState

class PlayerStateTest extends AnyFunSuite:


  val myPokemon = pokemonInitialState(MyPokedex.allPokemons.head)
  val mySecondPokemon = pokemonInitialState(MyPokedex.allPokemons(1))
  val player = playerState(Map("Pikachu" -> myPokemon, "Charmander" -> mySecondPokemon), "Pikachu")

  test("test base"):
    assert(player.activeId == "Pikachu")
    assert(player.team("Pikachu").currentHp == 10)
    assert(player.team("Charmander").currentHp == 10)

  test("test active"):
    val newPlayer = player active (_ damage 4)
    assert(newPlayer.team("Pikachu").currentHp == 6)
    assert(newPlayer.team("Charmander").currentHp == 10)

  test("test switch active"):
    val newPlayer = player switchActive "Charmander"
    assert(newPlayer.activeId == "Charmander")

  test("test bench"):
    val newPlayer = player bench (_ damage 3)
    assert(newPlayer.team("Pikachu").currentHp == 10)
    assert(newPlayer.team("Charmander").currentHp == 7)

  test("test all"):
    val newPlayer = player all (_ damage 2)
    assert(newPlayer.team("Pikachu").currentHp == 8)
    assert(newPlayer.team("Charmander").currentHp == 8)
    
  test("test allThat"):
    val newPlayer = player.allThat(ps => ps.currentHp < 10)(_ heal 2)
    assert(newPlayer.team("Pikachu").currentHp == 10)
    assert(newPlayer.team("Charmander").currentHp == 10)
