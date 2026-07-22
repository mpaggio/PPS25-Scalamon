package scalamon.logics.teambuilder

import scalamon.logics.teambuilder.TeamBuilder.*


/**
 * Implementation of [[TeamBuilder]] that allows for manual selection of Pokémon and moves.
 * It acts as a concrete "Strategy" within the team-building framework, enabling
 * users to define their own selection logic through higher-order functions.
 */
case class ManualTeamBuilder(
                              choosePokemonTeam: PokemonSelector,
                              chooseMoves: MoveSelector,
                              chooseItems: ItemSelector
                            ) extends TeamBuilder
