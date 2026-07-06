package scalamon.logics.teambuilder

import TeamBuilder.*

object AffineTeamBuilder:
  val numberOfSameTypeMoves = 2
  val numberOfAffineTypeMoves = 2

  case class AffineTeamBuilder() extends TeamBuilder:
    import scala.util.Random
    import scalamon.domain.moves.Move
    import scalamon.domain.moves.MoveDatabase.*
    import scalamon.domain.pokemon.Pokemon
    import scalamon.domain.types.Type
    import scalamon.domain.types.Type.values
    import scalamon.domain.types.TypeChart.effectiveness
    import scalamon.domain.types.TypeEffectiveness.{NotVeryEffective, SuperEffective}

    override def choosePokemonTeam(available: List[Pokemon]): List[Pokemon] =
      Random.shuffle(available).take(numberOfPokemonPerTeam)

    override def chooseMoves(pokemon: Pokemon): List[Move] =
      val myType: Type = pokemon.pokemonType
      val all: List[Move] = allMoves.toList
      val sameTypeMoves: List[Move] =
        Random.shuffle(all.filter(_.moveType == myType)).take(numberOfSameTypeMoves)
      val affineMoves: List[Move] =
        Random.shuffle(
          all.filter(m => checkIfMoveTypeIsAffine(m.moveType, myType))
        ).take(numberOfAffineTypeMoves)
      val selectedMovesSoFar = (sameTypeMoves ++ affineMoves).distinct
      handleFallback(selectedMovesSoFar, all)

    private def handleFallback(selectedSoFar: List[Move], all: List[Move]): List[Move] =
      if selectedSoFar.size < numberOfMovesPerPokemon then
        val remainingPool = all.filterNot(selectedSoFar.contains)
        selectedSoFar ++ Random.shuffle(remainingPool).take(numberOfMovesPerPokemon - selectedSoFar.size)
      else
        selectedSoFar.take(numberOfMovesPerPokemon)

    private def checkIfMoveTypeIsAffine(moveType: Type, myType: Type): Boolean =
      moveType != myType && values.exists(targetType =>
        effectiveness(myType, targetType) == NotVeryEffective &&
          effectiveness(moveType, targetType) == SuperEffective
      )