package scalamon.logics.state

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.StatsStateModuleImpl.*

class PlayerStateTest extends AnyWordSpec with Matchers with StateFixtures:
  "A PlayerState" should:

    "initialize correctly" in:
      player1.activeId shouldEqual "Charmander"
      player1.team("Charmander").currentHp shouldEqual 39
      player1.team("Bulbasaur").currentHp shouldEqual 45

    "find the active pokemon" in:
      val activePokemon = player1.getActive
      activePokemon shouldEqual player1.team("Charmander")

    "apply modifier to active pokemon" in:
      val newPlayer = player1 active (_ currentHp (_ decrease 10))
      newPlayer.team("Charmander").currentHp shouldEqual 29
      newPlayer.team("Bulbasaur").currentHp shouldEqual 45

    "switch active pokemon" in:
      val newPlayer = player1 switchActive "Bulbasaur"
      newPlayer.activeId shouldEqual "Bulbasaur"

    "apply modifier to benched pokemons" in:
      val newPlayer = player1 bench (_ currentHp (_ decrease 5))
      newPlayer.team("Charmander").currentHp shouldEqual 39
      newPlayer.team("Bulbasaur").currentHp shouldEqual 40

    "apply modifier to all pokemons" in:
      val newPlayer = player1 all (_ currentHp (_ decrease 5))
      newPlayer.team("Charmander").currentHp shouldEqual 34
      newPlayer.team("Bulbasaur").currentHp shouldEqual 40

    "apply modifier to filtered pokemons" in:
      val damagedPlayer = player1 all (_ currentHp (_ decrease 10)) // Pika 29, Char 35
      val healedPlayer = damagedPlayer.allThat(ps => ps.currentHp.toInt < 30)(_ currentHp (_ increase 10))
      healedPlayer.team("Charmander").currentHp shouldEqual 39
      healedPlayer.team("Bulbasaur").currentHp shouldEqual 35

