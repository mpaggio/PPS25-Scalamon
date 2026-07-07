package scalamon.logics.teambuilder

import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.moves.Move
import scalamon.domain.pokemon.Pokemon
import scalamon.logics.teambuilder.TeamBuilder.*
import scala.util.Random

class InvariantTeamBuilderTest extends org.scalatest.funsuite.AnyFunSuite:

  test(s"Random team builder should throw IllegalArgumentException if team size is not $numberOfPokemonPerTeam"):
    val brokenTeamBuilder = new TeamBuilder.TeamBuilder:
      override def choosePokemonTeam(available: List[Pokemon]): List[Pokemon] =
        available.take(numberOfPokemonPerTeam - 1)

      override def chooseMoves(pokemon: Pokemon, availableMoves: List[Move]): List[Move] =
        Random.shuffle(availableMoves).take(4)

    intercept[IllegalArgumentException]:
      brokenTeamBuilder.buildTeam()

  test(s"Random team builder should throw IllegalArgumentException if a Pokemon has less then $numberOfPokemonPerTeam moves"):
    val brokenTeamBuilder = new TeamBuilder.TeamBuilder:
      override def choosePokemonTeam(available: List[Pokemon]): List[Pokemon] =
        available.take(numberOfPokemonPerTeam)

      override def chooseMoves(pokemon: Pokemon, availableMoves: List[Move]): List[Move] =
        Random.shuffle(availableMoves).take(3)

    intercept[IllegalArgumentException]:
      brokenTeamBuilder.buildTeam()
