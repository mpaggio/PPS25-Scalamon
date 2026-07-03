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
      val newPlayer = active(currentHp(decrease(10)))(player1)
      newPlayer.team("Charmander").currentHp shouldEqual 29
      newPlayer.team("Bulbasaur").currentHp shouldEqual 45

    "switch active pokemon" in:
      val newPlayer = switchActive("Bulbasaur")(player1)
      newPlayer.activeId shouldEqual "Bulbasaur"

    "apply modifier to benched pokemons" in:
      val newPlayer = bench(currentHp(decrease(5)))(player1)
      newPlayer.team("Charmander").currentHp shouldEqual 39
      newPlayer.team("Bulbasaur").currentHp shouldEqual 40

    "apply modifier to all pokemons" in:
      val newPlayer = PlayerStateModuleImpl.all(currentHp(decrease(5)))(player1)
      newPlayer.team("Charmander").currentHp shouldEqual 34
      newPlayer.team("Bulbasaur").currentHp shouldEqual 40

    "apply modifier to filtered pokemons" in:
      val damagedPlayer = PlayerStateModuleImpl.all(currentHp(decrease(10)))(player1) // Pika 29, Char 35
      val healedPlayer = allThat(ps => ps.currentHp.toInt < 30)(currentHp(increase(10)))(damagedPlayer)
      healedPlayer.team("Charmander").currentHp shouldEqual 39
      healedPlayer.team("Bulbasaur").currentHp shouldEqual 35

