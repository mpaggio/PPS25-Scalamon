package scalamon.logics.teambuilder

import org.scalatest.matchers.should.Matchers.*
import scalamon.controller.GameConfig.*

class RandomTeamBuilderTest extends org.scalatest.funsuite.AnyFunSuite:

  test(s"Random team builder should create a team of exactly $TeamSize Pokemon"):
    val playerState = RandomTeamBuilder.buildTeam("Player1")
    playerState.team.size shouldBe TeamSize

  test(s"Random team builder should assign exactly $MovesPerPokemon moves to each Pokemon"):
    val playerState = RandomTeamBuilder.buildTeam("Player1")
    playerState.team.values.foreach(p => p.moves.size shouldBe MovesPerPokemon)

  test("Random team builder should select unique Pokemon names"):
    val playerState = RandomTeamBuilder.buildTeam("Player1")
    playerState.team.keys.toSet.size shouldBe TeamSize