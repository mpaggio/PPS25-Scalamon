package scalamon.domain.pokemon

import scalamon.domain.types.Type
import scalamon.domain.pokemon.PokedexADT.PokedexId
import scalamon.domain.pokemon.abilities.AbilitySlot
import scalamon.domain.pokemon.statistics.Stats

case class Pokemon(
  pokedexId: PokedexId,
  name: String,
  pokemonType: Type,
  baseStats: Stats,
  abilitySlot: AbilitySlot
):
  require(name.trim.nonEmpty, "Pokemon name must be a non-empty string")