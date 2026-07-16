package scalamon.logics.teambuilder

import TeamBuilder.*

/**
 * Singleton implementation of [[TeamBuilder]] that utilizes total randomness
 * for both Pokémon and move selection.
 *
 * It acts as a concrete "Strategy" within the team-building framework,
 * allowing for high variety in battle simulations.
 */
object RandomTeamBuilder extends TeamBuilder:


    import scala.util.Random
    /**
     * Randomly selects a team of unique Pokémon from the provided available pool.
     * It relies on the [[TeamBuilder]] trait to enforce the business invariant
     * that a team must contain of exactly size members.
     */
    override def choosePokemonTeam: PokemonSelector = (available, size) => Random.shuffle(available).take(size)

    /**
     * Selects size moves for a given Pokémon by sampling from the entire move database.
     * It uses the entire move database as a valid pool, ignoring type restrictions.
     */
    override def chooseMoves: MoveSelector = (_, availableMoves, size) => Random.shuffle(availableMoves).take(size)

    /**
     * Selects size items for the match by sampling from the entire item database.
     */
    override def chooseItems: ItemSelector = (availableItems, size) => Random.shuffle(availableItems).take(size)