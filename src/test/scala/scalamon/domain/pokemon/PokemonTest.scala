package scalamon.domain.pokemon

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.pokemon.Ability.*
import scalamon.domain.pokemon.PokedexADT.PokedexId
import scalamon.domain.types.Type.*

class PokemonTest extends AnyFunSuite:

  val testStats = Stats(hp = 39, attack = 52, defense = 43, specialAttack = 60, specialDefense = 50, speed = 65)
  val testAbilitySlot = AbilitySlot(Blaze, None, Some(SolarScales))
  val validPokedexId: PokedexId = PokedexADT.fromInt(1)

  test("A Pokemon with valid parameters and only 1 type should be correctly") {
    val charmander = Pokemon(
      pokedexId = validPokedexId,
      name = "Charmander",
      pokemonType = Fire,
      baseStats = testStats,
      abilitySlot = testAbilitySlot
    )
    charmander.pokedexId.toInt shouldBe 1
    charmander.name shouldBe "Charmander"
    charmander.pokemonType shouldBe Fire
    charmander.baseStats shouldBe testStats
    charmander.abilitySlot shouldBe testAbilitySlot
  }

  test("A pokemon created with a pokdexId <= 0 should throw an IllegalArgumentException") {
      a[IllegalArgumentException] should be thrownBy Pokemon(
        pokedexId = PokedexADT.fromInt(-1),
        name = "Charmander",
        pokemonType = Fire,
        baseStats = testStats,
        abilitySlot = testAbilitySlot
      )

      a[IllegalArgumentException] should be thrownBy Pokemon(
        pokedexId = PokedexADT.fromInt(0),
        name = "Charmander",
        pokemonType = Fire,
        baseStats = testStats,
        abilitySlot = testAbilitySlot
      )
  }

  test("A pokemon created with an empty name or an only whitespace name should throw an IllegalArgumentException") {
    a[IllegalArgumentException] should be thrownBy Pokemon(
      pokedexId = validPokedexId,
      name = "",
      pokemonType = Fire,
      baseStats = testStats,
      abilitySlot = testAbilitySlot
    )
    a[IllegalArgumentException] should be thrownBy Pokemon(
      pokedexId = validPokedexId,
      name = "   ",
      pokemonType = Fire,
      baseStats = testStats,
      abilitySlot = testAbilitySlot
    )
  }

