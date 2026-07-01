package scalamon.logics.state

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.StatsStateModuleImpl.*

class PokemonStateTest extends AnyWordSpec with Matchers with StateFixtures:
  "A PokemonState" should:

    "initialize with correct hp" in:
      myPokemon.currentHp shouldEqual 39

    "apply damage correctly" in:
      val damagedPokemon = currentHp(decrease(14))(myPokemon)
      damagedPokemon.currentHp shouldEqual 25

    "apply sequentially composed damage and heal correctly" in:
      val damagedPokemon = currentHp(decrease(14))(myPokemon)
      val healedPokemon = currentHp(increase(5))(damagedPokemon)
      healedPokemon.currentHp shouldEqual 30



