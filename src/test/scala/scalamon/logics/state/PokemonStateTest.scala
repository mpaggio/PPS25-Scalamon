package scalamon.logics.state

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalamon.logics.state.PokemonStateModuleImpl.*

class PokemonStateTest extends AnyWordSpec with Matchers with StateFixtures:
  "A PokemonState" should:

    "initialize with correct hp" in:
      myPokemon.currentHp shouldEqual 39

    "apply damage correctly" in:
      val damagedPokemon = myPokemon damage 14
      damagedPokemon.currentHp shouldEqual 25

    "apply sequentially composed damage and heal correctly" in:
      val damagedPokemon = myPokemon damage 14
      val healedPokemon = damagedPokemon heal 5
      healedPokemon.currentHp shouldEqual 30



