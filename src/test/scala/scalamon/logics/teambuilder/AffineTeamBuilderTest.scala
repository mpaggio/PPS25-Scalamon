package scalamon.logics.teambuilder

import org.scalatest.matchers.should.Matchers.*
import scalamon.database.MyPokedex.allPokemons
import scalamon.domain.types.Type.values
import scalamon.domain.types.TypeEffectiveness.*
import scalamon.domain.types.TypeChart.effectiveness
import scalamon.database.MoveDatabase.*
import scalamon.controller.GameConfig.*
import scalamon.logics.teambuilder.AffineTeamBuilder.numberOfSameTypeMoves

class AffineTeamBuilderTest extends org.scalatest.funsuite.AnyFunSuite:

  test(s"Affine team AffineTeamBuilder should create a team of exactly $TeamSize Pokemon"):
    val playerState = AffineTeamBuilder.buildTeam("Player1")
    playerState.team.size shouldBe TeamSize

  test(s"Affine team AffineTeamBuilder should assign exactly $MovesPerPokemon moves to each Pokemon"):
    val playerState = AffineTeamBuilder.buildTeam("Player1")
    playerState.team.values.foreach(p => p.moves.size shouldBe MovesPerPokemon)

  test("Affine team AffineTeamBuilder should select unique Pokemon names"):
    val playerState = AffineTeamBuilder.buildTeam("Player1")
    playerState.team.keys.toSet.size shouldBe TeamSize

  test("Affine team AffineTeamBuilder should prioritize STAB moves (same type)"):
    val charizard = allPokemons.find(_.name == "Charizard").get
    val chosenMoves = AffineTeamBuilder.chooseMoves(charizard, allMoves.toList, MovesPerPokemon)
    val fireMoves = chosenMoves.filter(_.moveType == charizard.pokemonType)
    fireMoves.size should be <= numberOfSameTypeMoves

  test("Affine team AffineTeamBuilder should select smart coverage moves"):
    val charizard = allPokemons.find(_.name == "Charizard").get
    val chosenMoves = AffineTeamBuilder.chooseMoves(charizard, allMoves.toList, MovesPerPokemon)
    val coverageMoves = chosenMoves.filter(_.moveType != charizard.pokemonType)
    coverageMoves.foreach(m =>
      val isAffine = values.exists(targetType =>
        effectiveness(charizard.pokemonType, targetType) == NotVeryEffective &&
          effectiveness(m.moveType, targetType) == SuperEffective
      )
      if (m.moveType != charizard.pokemonType)
        assert(isAffine, s"Move ${m.name} of type ${m.moveType} is not strategically affine to ${charizard.pokemonType}")
    )