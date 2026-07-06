package scalamon.logics.teambuilder

import TeamBuilder.*

object RandomTeamBuilder:

  case class RandomTeamBuilder() extends TeamBuilder:
    import scala.util.Random
    import scalamon.domain.moves.Move
    import scalamon.domain.moves.MoveDatabase.*
    import scalamon.domain.pokemon.Pokemon

    override def choosePokemonTeam(available: List[Pokemon]): List[Pokemon] =
      Random.shuffle(available).take(numberOfPokemonPerTeam)

    override def chooseMoves(pokemon: Pokemon): List[Move] =
      Random.shuffle(allMoves.toList).take(numberOfMovesPerPokemon)