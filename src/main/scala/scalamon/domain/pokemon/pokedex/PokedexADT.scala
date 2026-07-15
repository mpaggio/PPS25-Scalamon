package scalamon.domain.pokemon.pokedex

/**
 * Module defining the Pokédex identifier as an opaque type.
 * Ensures the invariant that an ID cannot be less than or equal to zero.
 */
object PokedexADT:
  opaque type PokedexId = Int

  def fromInt(id: Int): PokedexId =
    require(id > 0, "Pokedex ID must be a positive integer")
    id

  extension (id: PokedexId)
    def toInt: Int = id
