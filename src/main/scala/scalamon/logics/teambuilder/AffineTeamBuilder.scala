package scalamon.logics.teambuilder

import TeamBuilder.*

/**
 * Singleton implementation of [[TeamBuilder]] that builds teams using
 * affinity and offensive coverage heuristics.
 *
 * It implements the Strategy pattern for team generation and integrates
 * with the Template Method defined in the base trait.
 */
object AffineTeamBuilder extends TeamBuilder:
  /** Designed number of STAB (Same Type Attack Bonus) moves to assign before selecting coverage moves.*/
  val numberOfSameTypeMoves = 2

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
   * 1. Attempts to assign up to two STAB (Same Type Attack Bonus) moves.
   * 2. Attempts to assign the remaining coverage moves, that are super effective against types that resists to the Pokémon's type.
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
   * Randomly selects size distinct items from the available pool.
   */
  override def chooseItems: ItemSelector = (availableItems, size) => Random.shuffle(availableItems).take(size)

  /**
   * Mechanism that guarantees compliance with the size-move invariant.
   * If the affinity logic fails to find enough moves, it fills the remaining
   * slots with random moves from the general database.
   *
   * @param selectedSoFar Moves selected so far by the smart logic.
   * @param all The entire pool of moves available in the database.
   * @return A complete list containing exactly size moves.
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
   * The algorithm is computed directly from the type effectiveness table.
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