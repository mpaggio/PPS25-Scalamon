package scalamon.logics.teambuilder

import scalamon.domain.moves.Move
import scalamon.domain.pokemon.Pokemon
import scalamon.logics.teambuilder.TeamBuilder.TeamBuilder

/**
 * Companion object for ManualTeamBuilder.
 * Provides a namespace for the manual team generation strategy.
 */
object ManualTeamBuilder:

  /**
   * Implementation of [[TeamBuilder]] that allows for manual selection of Pokémon and moves.
   * It acts as a concrete "Strategy" within the team-building framework, enabling
   * users to define their own selection logic through higher-order functions.
   * @param pokemonSelector A function that takes a list of available Pokémon and returns a selected list of Pokémon for the team.
   * @param moveSelector A function that takes a Pokémon and a list of available moves, returning a selected list of moves for that Pokémon.
   */
  case class ManualTeamBuilder(
    pokemonSelector: List[Pokemon] => List[Pokemon],
    moveSelector: (Pokemon, List[Move]) => List[Move]
  ) extends TeamBuilder:

    /**
     * Selects a team of Pokémon based on the provided `pokemonSelector` function.
     * @param available The list of all Pokémon available in the domain database.
     * @return A list of Pokémon to be included in the team.
     */
    override def choosePokemonTeam(available: List[Pokemon]): List[Pokemon] =
      pokemonSelector(available)

    /**
     * Selects moves for a given Pokémon based on the provided `moveSelector` function.
     * @param pokemon The Pokémon for which moves are being selected.
     * @param availableMoves The list of all move available in the domain database.
     * @return A list of moves to be assigned to the Pokémon.
     */
    override def chooseMoves(pokemon: Pokemon, availableMoves: List[Move]): List[Move] =
      moveSelector(pokemon, availableMoves)