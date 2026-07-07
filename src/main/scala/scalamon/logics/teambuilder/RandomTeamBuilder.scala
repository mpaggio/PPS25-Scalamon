package scalamon.logics.teambuilder

import TeamBuilder.*

/**
 * Companion object for RandomTeamBuilder.
 * Provides a namespace for the random team generation strategy.
 */
object RandomTeamBuilder:

  /**
   * Implementation of [[TeamBuilder]] that utilizes total randomness
   * for both Pokémon and move selection.
   *
   * It acts as a concrete "Strategy" within the team-building framework,
   * allowing for high variety in battle simulations.
   */
  case class RandomTeamBuilder() extends TeamBuilder:
    import scala.util.Random
    import scalamon.domain.moves.Move
    import scalamon.domain.pokemon.Pokemon

    /**
     * Randomly selects a team of unique Pokémon from the provided available pool.
     * It relies on the [[TeamBuilder]] trait to enforce the business invariant
     * that a team must contain of exactly 6 members.
     *
     * @param available The list of all available Pokémon in the Pokédex.
     * @return A randomized list of 6 unique Pokémon.
     */
    override def choosePokemonTeam(available: List[Pokemon]): List[Pokemon] =
      Random.shuffle(available).take(numberOfPokemonPerTeam)

    /**
     * Selects 4 moves for a given Pokémon by sampling from the entire move database.
     * It uses the entire move database as a valid pool, ignoring type restrictions.
     *
     * @param pokemon The Pokémon for which moves are being selected.
     * @param availableMoves The list of all move available in the domain database.
     * @return A randomized list of exactly 4 moves.
     */
    override def chooseMoves(pokemon: Pokemon, availableMoves: List[Move]): List[Move] =
      Random.shuffle(availableMoves).take(numberOfMovesPerPokemon)