package scalamon.domain.pokemon

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.pokemon.pokedex.PokedexADT.PokedexId
import scalamon.domain.pokemon.abilities.Ability.*
import scalamon.domain.pokemon.abilities.AbilitySlot
import scalamon.domain.pokemon.pokedex.PokedexADT
import scalamon.domain.pokemon.statistics.StatADT.*
import scalamon.domain.pokemon.statistics.Stats
import scalamon.domain.types.Type.*

class PokemonTest extends AnyFunSuite:

  val testStats = Stats(hp = fromInt(39), attack = fromInt(52), defense = fromInt(43), specialAttack = fromInt(60), specialDefense = fromInt(50), speed = fromInt(65))
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

  test("A pokemon created with Normal Type should throw an IllegalArgumentException") {
    a[IllegalArgumentException] should be thrownBy Pokemon(
      pokedexId = validPokedexId,
      name = "NormalPokemon",
      pokemonType = Normal,
      baseStats = testStats,
      abilitySlot = testAbilitySlot
    )
  }

