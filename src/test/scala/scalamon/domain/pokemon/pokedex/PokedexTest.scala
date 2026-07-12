package scalamon.domain.pokemon.pokedex

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.types.Type
import scalamon.domain.types.Type.*
import scalamon.domain.pokemon.abilities.Ability.*
import scalamon.domain.pokemon.pokedex.PokedexDSL.*

class PokedexTest extends AnyFunSuite:

  test("The Pokedex should correctly load the list of Pokemon and not be empty") {
    val pokedex = MyPokedex.allPokemons
    pokedex should not be empty

    pokedex.exists(_.name == "Charmander") shouldBe true
    pokedex.exists(_.name == "Bulbasaur") shouldBe true
    pokedex.exists(_.name == "Giratina") shouldBe false
  }

  test("The DSL should map correctly a pokemon verifying all of its fields") {
    val charmanderOpt = MyPokedex.allPokemons.find(_.name == "Charmander")
    charmanderOpt.isDefined shouldBe true

    val charmander = charmanderOpt.get

    charmander.pokedexId.toInt shouldBe 4
    charmander.pokedexId.toInt should not be 5
    charmander.pokemonType shouldBe Fire
    charmander.pokemonType should not be Water
    charmander.baseStats.hp.toInt shouldBe 39
    charmander.baseStats.attack.toInt shouldBe 52
    charmander.baseStats.defense.toInt shouldBe 43
    charmander.abilitySlot.primary shouldBe Blaze
    charmander.abilitySlot.secondary shouldBe None
    charmander.abilitySlot.hidden shouldBe Some(SolarScales)
  }

  test("The DSL's extension methods should correctly assign one between secondary and hidden abilities to a Pokemon") {
    val squirtle = MyPokedex.allPokemons.find(_.name == "Squirtle").get

    squirtle.pokemonType shouldBe Water
    squirtle.abilitySlot.primary shouldBe Torrent
    squirtle.abilitySlot.secondary shouldBe None
    squirtle.abilitySlot.hidden shouldBe Some(Hydration)
  }

  test("The DSL should prevent assigning both a secondary and a hidden ability to the same Pokemon") {
    a[IllegalArgumentException] should be thrownBy {
      given dummyType: Type = Fire
      "DummyPokemon" id 999 stats(10, 10, 10, 10, 10, 10) ability RunAway withSecondaryAbility Intimidate withHiddenAbility ShedSkin
    }
  }