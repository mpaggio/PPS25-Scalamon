package scalamon.logics.teambuilder

import org.scalatest.matchers.should.Matchers.*
import scalamon.logics.teambuilder.RandomTeamBuilder.RandomTeamBuilder
import scalamon.logics.teambuilder.TeamBuilder.*

class RandomTeamBuilderTest extends org.scalatest.funsuite.AnyFunSuite:
  val builder = RandomTeamBuilder()

  test(s"Random team builder should create a team of exactly $numberOfPokemonPerTeam Pokemon"):
    val playerState = builder.buildTeam("Player1")
    playerState.team.size shouldBe numberOfPokemonPerTeam

  test(s"Random team builder should assign exactly $numberOfMovesPerPokemon moves to each Pokemon"):
    val playerState = builder.buildTeam("Player1")
    playerState.team.values.foreach(p => p.moves.size shouldBe numberOfMovesPerPokemon)

  test("Random team builder should select unique Pokemon names"):
    val playerState = builder.buildTeam("Player1")
    playerState.team.keys.toSet.size shouldBe numberOfPokemonPerTeam