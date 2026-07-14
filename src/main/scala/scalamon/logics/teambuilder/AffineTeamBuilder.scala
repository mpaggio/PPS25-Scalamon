package scalamon.logics.teambuilder

import TeamBuilder.*

/**
 * Companion object for AffineTeamBuilder.
 * Contains balancing constants for the move selection strategy.
 */
object AffineTeamBuilder:
  /** Number of moves of the same type as the Pokémon (STAB) to attempt to assign. */
  val numberOfSameTypeMoves = 2

  /**
   * Implementation of [[TeamBuilder]] that uses a "smart" strategy
   * based on type affinity and offensive coverage.
   *
   * It follows the "Strategy" design pattern for team generation and
   * integrates into the "Template Method" defined in the base trait.
   */
  case class AffineTeamBuilder() extends TeamBuilder:
    import scala.util.Random
    import scalamon.domain.moves.Move
    import scalamon.domain.types.Type
    import scalamon.domain.types.Type.values
    import scalamon.domain.types.TypeChart.effectiveness
    import scalamon.domain.types.TypeEffectiveness.{NotVeryEffective, SuperEffective}

    /**
     * Randomly selects a team of unique Pokémon from the available database.
     */
    override def choosePokemonTeam: PokemonSelector = (available, size) => Random.shuffle(available).take(size)

    /**
     * Selects size moves for the Pokémon, following tactical efficacy criteria:
     * 1. Attempts to assign 2 STAB (Same Type Attack Bonus) moves.
     * 2. Attempts to assign remaning coverage moves (SuperEffective against types that resists to the Pokémon's type).
     * 3. Uses a fallback mechanism to ensure the requirement of exactly size moves is met.
     */
    override def chooseMoves: MoveSelector = (pokemon, availableMoves, size) =>
      val myType: Type = pokemon.pokemonType
      val sameTypeMoves: List[Move] =
        Random.shuffle(availableMoves.filter(_.moveType == myType)).take(numberOfSameTypeMoves)
      val affineMoves: List[Move] =
        Random.shuffle(
          availableMoves.filter(m => checkIfMoveTypeIsAffine(m.moveType, myType))
        ).take(size - numberOfSameTypeMoves)
      val selectedMovesSoFar = (sameTypeMoves ++ affineMoves).distinct
      handleFallback(selectedMovesSoFar, availableMoves, size: Int)

    /**
     * Randomly selects a set of items from the available pool.
     */
    override def chooseItems: ItemSelector = (availableItems, size) => Random.shuffle(availableItems).take(size)

    /**
     * Mechanism that guarantee compliance with the size-move invariant.
     * If the affinity logic fails to find enough moves, it fills the remaining
     * slots with random moves from the general database.
     *
     * @param selectedSoFar Move selected so far by the smart logic.
     * @param all The entire pool of moves available in the database.
     * @return A complete list of exactly size moves.
     */
    private def handleFallback(selectedSoFar: List[Move], all: List[Move], size: Int): List[Move] =
      if selectedSoFar.size < size then
        val remainingPool = all.filterNot(selectedSoFar.contains)
        selectedSoFar ++ Random.shuffle(remainingPool).take(size - selectedSoFar.size)
      else
        selectedSoFar.take(size)

    /**
     * Implements the strategic coverage criterion by querying the domain model.
     * A move is considered "affine" if its type is SuperEffective against at least one type
     * that the current Pokémon is NotVeryEffective against.
     * The algorithm derives relationships directly from the type table.
     *
     * @param moveType The type of the move to evaluate.
     * @param myType The type of the Pokémon learning the move.
     * @return True if the move offers strategic coverage, False otherwise.
     */
    private def checkIfMoveTypeIsAffine(moveType: Type, myType: Type): Boolean =
      moveType != myType && values.exists(targetType =>
        effectiveness(myType, targetType) == NotVeryEffective &&
          effectiveness(moveType, targetType) == SuperEffective
      )