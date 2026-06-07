package scalamon.domain.pokemon

object PokedexADT:
  opaque type PokedexId = Int

  def fromInt(id: Int): PokedexId =
    require(id > 0, "Pokedex ID must be a positive integer")
    id

  extension (id: PokedexId)
    def toInt: Int = id
