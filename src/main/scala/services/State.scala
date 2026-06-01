package services

import services.Match.PokemonInMatch

object State:
  
  case class GameState(
                      players: (PlayerState, PlayerState), // List[Player]
                      weather: Weather,
                      epoch: Int,
                      )
  
  case class PlayerState(
                   pokemons: List[PokemonState],
                   activePokemon: Int,
                   )
  
  case class PokemonState(
                           pokemon: PokemonInMatch,
                           currentHP: Int,
                           stats: Stats, // ???
                           alterateStatus: List[StatusAlteration],
                           visible: Boolean,
                         )
