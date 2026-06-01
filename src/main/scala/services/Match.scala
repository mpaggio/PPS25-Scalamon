package services

object Match:
  case class PokemonInMatch(
                             pokemonSpiecies: Pokemon,
                             moves: List[Move],
                             accessory: Option[Accessory],
                           )

  case class Player(
                   name: String,
                   // pokemons: List[PokemonInMatch], ??
                 )