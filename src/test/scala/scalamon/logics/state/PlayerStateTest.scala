package scalamon.logics.state

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*

class PlayerStateTest extends AnyWordSpec with Matchers with StateFixtures:
  "A PlayerState" should:

    "initialize correctly" in:
      player1.activeId shouldEqual "Pikachu"
      player1.team("Pikachu").currentHp shouldEqual 39
      player1.team("Charmander").currentHp shouldEqual 45

    "apply modifier to active pokemon" in:
      val newPlayer = player1 active (_ damage 10)
      newPlayer.team("Pikachu").currentHp shouldEqual 29
      newPlayer.team("Charmander").currentHp shouldEqual 45

    "switch active pokemon" in:
      val newPlayer = player1 switchActive "Charmander"
      newPlayer.activeId shouldEqual "Charmander"

    "apply modifier to benched pokemons" in:
      val newPlayer = player1 bench (_ damage 5)
      newPlayer.team("Pikachu").currentHp shouldEqual 39
      newPlayer.team("Charmander").currentHp shouldEqual 40

    "apply modifier to all pokemons" in:
      val newPlayer = player1 all (_ damage 5)
      newPlayer.team("Pikachu").currentHp shouldEqual 34
      newPlayer.team("Charmander").currentHp shouldEqual 40

    "apply modifier to filtered pokemons" in:
      val damagedPlayer = player1 all (_ damage 10) // Pika 29, Char 35
      val healedPlayer = damagedPlayer.allThat(ps => ps.currentHp < 30)(_ heal 10)
      healedPlayer.team("Pikachu").currentHp shouldEqual 39
      healedPlayer.team("Charmander").currentHp shouldEqual 35

