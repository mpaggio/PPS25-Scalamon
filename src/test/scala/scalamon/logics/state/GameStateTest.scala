package scalamon.logics.state

import org.scalatest.funsuite.AnyFunSuite
import scalamon.domain.pokemon.statistics.StatADT.*
import scalamon.domain.pokemon.statistics.Stats
import scalamon.domain.pokemon.pokedex.MyPokedex
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.StatsStateModuleImpl.*

class GameStateTest extends AnyFunSuite:
  test("general test"):   // TODO: split into multiple tests

    type Move = BattleState => BattleState

    val myPokemon = pokemonInitialState(MyPokedex.allPokemons.head)
    val mySecondPokemon = pokemonInitialState(MyPokedex.allPokemons(1))
    val enemyPokemon = pokemonInitialState(MyPokedex.allPokemons(2))
    val player1 = playerState(Map("Pikachu" -> myPokemon,"Charmander" -> mySecondPokemon), "Pikachu")
    val player2 = playerState(Map("Bulbasaur" -> enemyPokemon), "Bulbasaur")
    val state = battleState(player1, player2)

    val attackMove: Move = _ enemy (_ active (_ damage 4))

    val masochistMove: Move = _ user (_ bench (_ damage 3))

    val healAllMove: Move = _ user (_.allThat(ps => ps.currentHp < 10)(_ heal 2))

    val weaknessMove: Move = _ enemy (_ active (_ modifyStats (_ attack (_ decrease 2))))

    //val burn: Move = _ enemy (_ active (_.addStatus(_ duration 3)(_ damage 4)  ))
    
    //val paralaized = _ enemy (_ active (_ addStatus (moves (_ accuracy (_ decrease 1)))

    assert(state._2.team("Bulbasaur").currentHp == 10)
    val newState = attackMove andThen masochistMove andThen healAllMove andThen weaknessMove apply state
    assert(newState._2.team("Bulbasaur").currentHp == 6)
    assert(newState._1.team("Pikachu").currentHp == 10)
    assert(newState._1.team("Charmander").currentHp == 9)
    assert(newState._2.team("Bulbasaur").modifiedStats.attack == fromInt(4))
