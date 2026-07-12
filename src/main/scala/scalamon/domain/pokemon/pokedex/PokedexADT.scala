package scalamon.domain.pokemon.pokedex

/**
 * Module defining the Pokédex identifier as an opaque type.
 * Ensures the invariant that an ID cannot be less than or equal to zero.
 */
object PokedexADT:
  opaque type PokedexId = Int

  /**
   * Creates a PokedexId from an integer, enforcing that the ID must be a positive integer.
   * @param id the integer value to be converted into a PokedexId
   * @return a PokedexId representing the given integer
   * @throws IllegalArgumentException if the provided id is less than or equal to zero
   */
  def fromInt(id: Int): PokedexId =
    require(id > 0, "Pokedex ID must be a positive integer")
    id

  /**
   * Extension method to convert a PokedexId back to an integer.
   * @return the integer representation of the PokedexId
   */
  extension (id: PokedexId)
    def toInt: Int = id
