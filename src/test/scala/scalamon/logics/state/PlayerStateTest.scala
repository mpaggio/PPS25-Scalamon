package scalamon.logics.state

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalamon.logics.state.PlayerStateModuleImpl.*

class PlayerStateTest extends AnyWordSpec with Matchers with PlayerFixtures:

  private def hp(f: Int => Int): PokemonState => PokemonState = pk => pk.copy(currentHp = f(pk.currentHp))

  "A PlayerState" should:

    "initialize with team, active pokemon and no items" in:
      player1.activeId shouldEqual "Charmander"
      player1.team.keySet shouldEqual Set("Charmander", "Bulbasaur")
      player1.items shouldBe empty

    "find the active pokemon" in:
      player1.getActive shouldEqual player1.team("Charmander")

    "switch the active pokemon" in:
      switchActive("Bulbasaur")(player1).getActive shouldEqual player1.team("Bulbasaur")

    "apply a modifier to the active pokemon only" in:
      val p = active(hp(_ - 10))(player1)
      p.team("Charmander").currentHp shouldEqual 29
      p.team("Bulbasaur") shouldEqual player1.team("Bulbasaur")

    "apply a modifier to all pokemons" in:
      val p = PlayerStateModuleImpl.all(hp(_ - 5))(player1)
      p.team("Charmander").currentHp shouldEqual 34
      p.team("Bulbasaur").currentHp shouldEqual 40

    "apply a modifier only where the predicate holds" in:
      val p = allThat(pk => pk.currentHp < 40)(hp(_ + 10))(player1)
      p.team("Charmander").currentHp shouldEqual 49
      p.team("Bulbasaur") shouldEqual player1.team("Bulbasaur")

    "apply a modifier to benched pokemons only" in :
      val p = bench(hp(_ - 5))(player1)
      p.team("Charmander") shouldEqual player1.team("Charmander")
      p.team("Bulbasaur").currentHp shouldEqual 40

    "apply the update function to the items" in:
      var applied = false
      val p = items { s => applied = true; s }(player1)
      applied shouldBe true
      p.items shouldBe empty

    "update the battle flags" in:
      val p = updateFlags(_.copy(isSwitchBlocked = true))(player1)
      p.flags.isSwitchBlocked shouldBe true
      p.flags.flashFireActive shouldBe false
