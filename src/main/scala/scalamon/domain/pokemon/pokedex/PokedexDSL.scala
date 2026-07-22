package scalamon.domain.pokemon.pokedex

import scalamon.domain.pokemon.abilities.{Ability, AbilitySlot}
import scalamon.domain.pokemon.statistics.StatADT.fromInt
import scalamon.domain.pokemon.statistics.Stats
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.types.Type

/**
 * Module providing a Domain-Specific Language (DSL) for the hierarchical instantiation of a Pokémon.
 */
object PokedexDSL:

  /**
   * Aggregates various Pokémon categories into a single flat list, used in MyPokedex to define the complete set of Pokémon.
   * @param categories a variable number of lists of Pokémon, each representing a category.
   * @return a single list containing all Pokémon from the provided categories.
   */
  def Pokedex(categories: List[Pokemon]*): List[Pokemon] =
    categories.flatten.toList

  /**
   * Defines a category block for Pokémon of a specific type.
   * The type is implicitly injected into the context for all Pokémon defined within the block.
   * @param pokemonType the type of Pokémon in this category.
   * @param pokemon a variable number of Pokémon builder functions, each creating a Pokémon instance using the provided type.
   * @return a list of Pokémon instantiated with the given type and their respective attributes.
   */
  def Category(pokemonType: Type)(pokemon: (Type ?=> Pokemon)*): List[Pokemon] =
    pokemon.map(builderFunction => builderFunction(using pokemonType)).toList

  /**
   * Extension method to initiate the Pokémon building process by specifying the name and Pokédex ID.
   * @return an intermediate builder step that allows specifying the Pokémon's stats.
   */
  extension (name: String)

    /**
     * Infix method to specify the Pokédex ID for a Pokémon
     * @param pokedexId the Pokédex ID of the Pokémon
     * @return an instance of PokemonBuilderStep1, which is the next step that allows specifying the Pokémon's stats
     */
    infix def id(pokedexId: Int): PokemonBuilderStep1 =
      PokemonBuilderStep1(name, pokedexId)

  /**
   * Intermediate builder step for creating a Pokémon, allowing the specification of its stats.
   * I made the class visible only within the Pokédex package to prevent direct instantiation outside the DSL flow.
   * @param name the name of the Pokémon.
   * @param id the Pokédex ID of the Pokémon.
   * @return an instance of PokemonBuilderStep2, which is the next step that allows specifying the Pokémon's ability
   */
  private [pokedex] case class PokemonBuilderStep1(name: String, id: Int):
    infix def stats(hp: Int, attack: Int, defense: Int, specialAttack: Int, specialDefense: Int, speed: Int): PokemonBuilderStep2 =
      val s = Stats(
        fromInt(hp), fromInt(attack), fromInt(defense),
        fromInt(specialAttack), fromInt(specialDefense), fromInt(speed)
      )
      PokemonBuilderStep2(name, id, s)

  /**
   * Second intermediate builder step for creating a Pokémon, allowing the specification of its primary ability.
   * @param name the name of the Pokémon
   * @param id the Pokédex ID of the Pokémon
   * @param stats the base stats of the Pokémon
   * @return an instance of Pokémon, with the addition of the AbilitySlot(only the primary ability (mandatory)) and Type
   */
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

    /**
     * Extension method to add a secondary ability to an existing Pokémon instance.
     * @param secondaryAbility the secondary ability to be added to the Pokémon
     * @return a new Pokémon instance with the secondary ability set in its AbilitySlot
     */
    infix def withSecondaryAbility(secondaryAbility: Ability): Pokemon =
      p.copy(abilitySlot = p.abilitySlot.copy(secondary = Some(secondaryAbility)))

    /**
     * Extension method to add a hidden ability to an existing Pokémon instance.
     * @param hiddenAbility the hidden ability to be added to the Pokémon
     * @return a new Pokémon instance with the hidden ability set in its AbilitySlot
     */
    infix def withHiddenAbility(hiddenAbility: Ability): Pokemon =
      p.copy(abilitySlot = p.abilitySlot.copy(hidden = Some(hiddenAbility)))