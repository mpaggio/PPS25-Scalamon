package scalamon.logics.state

import org.scalatest.funsuite.AnyFunSuite
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*

class GameStateTest extends AnyFunSuite:
  test("general test"):   // TODO: split into multiple tests

    type Move = BattleState => BattleState

    val myPokemon = pokemonState(10)
    val mySecondPokemon = pokemonState(10)
    val enemyPokemon = pokemonState(10)
    val player1 = playerState(Map("Pikachu" -> myPokemon,"Charmander" -> mySecondPokemon), "Pikachu")
    val player2 = playerState(Map("Bulbasaur" -> enemyPokemon), "Bulbasaur")
    val state = battleState(player1, player2)

    val attackMove: Move = _ enemy (_ active (_ damage 4))

    val masochistMove: Move = _ user (_ bench (_ damage 3))

    val healAllMove: Move = _ user (_.allThat(ps => ps.hp < 10)(_ heal 2))

    assert(state._2.team("Bulbasaur").hp == 10)
    val newState = attackMove andThen masochistMove andThen healAllMove apply state
    assert(newState._2.team("Bulbasaur").hp == 6)
    assert(newState._1.team("Pikachu").hp == 10)
    assert(newState._1.team("Charmander").hp == 9)
