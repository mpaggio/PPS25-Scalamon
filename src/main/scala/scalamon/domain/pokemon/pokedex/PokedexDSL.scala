package scalamon.domain.pokemon.pokedex

import scalamon.domain.pokemon.abilities.{Ability, AbilitySlot}
import scalamon.domain.pokemon.statistics.StatADT.fromInt
import scalamon.domain.pokemon.statistics.Stats
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.types.Type

/**
 * Module providing a Domain-Specific Language (DSL) for the hierarchical
 * instantiation of Pokémon.
 * Uses extension methods and contextual abstractions to enable a declarative.
 * syntax that reflects the Pokédex categories.
 */
object PokedexDSL:

  /**
   * Aggregates various Pokémon categories into a single flat list
   * @param categories a variable number of lists of Pokémon, each representing a category.
   * @return a single list containing all Pokémon from the provided categories.
   */
  def Pokedex(categories: List[Pokemon]*): List[Pokemon] =
    categories.flatten.toList

  /**
   * Defines a category block for Pokémon of a specific type.
   * The type is implicitly injected into the context for all Pokémon
   * defined within the block.
   * @param pokemonType the type of Pokémon in this category.
   * @param pokemons a variable number of Pokémon builder functions that will be evaluated
   * using the provided type as an implicit parameter.
   * @return a list of Pokémon instantiated with the given type and their respective attributes.
   */
  def Category(pokemonType: Type)(pokemons: (Type ?=> Pokemon)*): List[Pokemon] =
    pokemons.map(builderFunction => builderFunction(using pokemonType)).toList

  extension (name: String)
    infix def id(pokedexId: Int): PokemonBuilderStep1 =
      PokemonBuilderStep1(name, pokedexId)

  /* Here I made the class visible only within the Pokédex package to prevent direct instantiation outside the DSL flow. */
  private [pokedex] case class PokemonBuilderStep1(name: String, id: Int):
    infix def stats(hp: Int, attack: Int, defense: Int, specialAttack: Int, specialDefense: Int, speed: Int): PokemonBuilderStep2 =
      val s = Stats(
        fromInt(hp), fromInt(attack), fromInt(defense),
        fromInt(specialAttack), fromInt(specialDefense), fromInt(speed)
      )
      PokemonBuilderStep2(name, id, s)

  case class PokemonBuilderStep2(name: String, id: Int, stats: Stats):
    infix def ability(primaryAbility: Ability)(using pokemonType: Type): Pokemon =
      Pokemon(
        pokedexId = PokedexADT.fromInt(id),
        name = name,
        pokemonType = pokemonType,
        baseStats = stats,
        abilitySlot = AbilitySlot(primaryAbility)
      )

  extension (p: Pokemon)

    infix def withSecondaryAbility(secondaryAbility: Ability): Pokemon =
      p.copy(abilitySlot = p.abilitySlot.copy(secondary = Some(secondaryAbility)))

    infix def withHiddenAbility(hiddenAbility: Ability): Pokemon =
      p.copy(abilitySlot = p.abilitySlot.copy(hidden = Some(hiddenAbility)))