package scalamon.logics.state

import org.scalatest.funsuite.AnyFunSuite
import scalamon.domain.pokemon.statistics.StatADT.*
import scalamon.domain.pokemon.statistics.Stats
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.StatModule.*
import scalamon.logics.state.StatsStateModuleImpl.*

class GameStateTest extends AnyFunSuite:
  test("general test"):   // TODO: split into multiple tests

    type Move = BattleState => BattleState
    
    val baseStats = statState(Stats(
      hp = fromInt(10),
      attack = fromInt(6),
      defense = fromInt(3),
      specialAttack = fromInt(4),
      specialDefense = fromInt(2),
      speed = fromInt(6)
    ))

    val myPokemon = pokemonState(10, baseStats)
    val mySecondPokemon = pokemonState(10, baseStats)
    val enemyPokemon = pokemonState(10, baseStats)
    val player1 = playerState(Map("Pikachu" -> myPokemon,"Charmander" -> mySecondPokemon), "Pikachu")
    val player2 = playerState(Map("Bulbasaur" -> enemyPokemon), "Bulbasaur")
    val state = battleState(player1, player2)

    val attackMove: Move = _ enemy (_ active (_ damage 4))

    val masochistMove: Move = _ user (_ bench (_ damage 3))

    val healAllMove: Move = _ user (_.allThat(ps => ps.hp < 10)(_ heal 2))

    val weaknessMove: Move = _ enemy (_ active (_ stats (_ attack (_ decrease 2))))

    //val paralaized = _ enemy (_ active (_ addStatus ()))

    assert(state._2.team("Bulbasaur").hp == 10)
    val newState = attackMove andThen masochistMove andThen healAllMove andThen weaknessMove apply state
    assert(newState._2.team("Bulbasaur").hp == 6)
    assert(newState._1.team("Pikachu").hp == 10)
    assert(newState._1.team("Charmander").hp == 9)
    assert(newState._2.team("Bulbasaur").stats.attack == fromInt(4))
