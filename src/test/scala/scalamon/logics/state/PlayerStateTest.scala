package scalamon.logics.state

import org.scalatest.funsuite.AnyFunSuite
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*

class PlayerStateTest extends AnyFunSuite:
  val player: Ps = playerState(Map("Pikachu" -> pokemonState(10), "Charmander" -> pokemonState(10)), "Pikachu")

  test("test base"):
    assert(player.activeId == "Pikachu")
    assert(player.team("Pikachu").hp == 10)
    assert(player.team("Charmander").hp == 10)

  test("test active"):
    val newPlayer = player active (_ damage 4)
    assert(newPlayer.team("Pikachu").hp == 6)
    assert(newPlayer.team("Charmander").hp == 10)

  test("test switch active"):
    val newPlayer = player switchActive "Charmander"
    assert(newPlayer.activeId == "Charmander")

  test("test bench"):
    val newPlayer = player bench (_ damage 3)
    assert(newPlayer.team("Pikachu").hp == 10)
    assert(newPlayer.team("Charmander").hp == 7)

  test("test all"):
    val newPlayer = player all (_ damage 2)
    assert(newPlayer.team("Pikachu").hp == 8)
    assert(newPlayer.team("Charmander").hp == 8)
    
  test("test allThat"):
    val newPlayer = player.allThat(ps => ps.hp < 10)(_ heal 2)
    assert(newPlayer.team("Pikachu").hp == 10)
    assert(newPlayer.team("Charmander").hp == 10)
