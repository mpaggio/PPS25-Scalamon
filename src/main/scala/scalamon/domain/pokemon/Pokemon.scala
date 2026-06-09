package scalamon.domain.pokemon

import scalamon.domain.types.Type
import scalamon.domain.pokemon.pokedex.PokedexADT.PokedexId
import scalamon.domain.pokemon.abilities.AbilitySlot
import scalamon.domain.pokemon.statistics.Stats

/**
 * Represents an immutable Pokémon entity.
 * Thic class is the static model of the Pokémon and does not contain
 * the logic of the combat, which will be handled in a dynamic model.
 * @param pokedexId the unique identifier of the Pokèmon in the Pokèdex.
 * @param name the name of the Pokèmon,
 * @param pokemonType the elemental type of the Pokèmon.
 * @param baseStats the base statistics of the Pokèmon.
 * @param abilitySlot the slots of the abilities of the Pokémon.
 * @throws IllegalArgumentException if the name is empty or contains only whitespace.
 */
case class Pokemon(
  pokedexId: PokedexId,
  name: String,
  pokemonType: Type,
  baseStats: Stats,
  abilitySlot: AbilitySlot
):
  require(name.trim.nonEmpty, "Pokemon name must be a non-empty string")