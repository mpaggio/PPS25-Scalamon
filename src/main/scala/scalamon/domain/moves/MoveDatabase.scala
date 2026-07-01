package scalamon.domain.moves

import scalamon.domain.types.Type
import scalamon.domain.types.Type.*
import DamageMoveCategory.*
import StatusMoveCategory.*
import MoveDSL.*
import MoveEffectDSL.*
import MoveEffectDSL.Effect.*
import AlteredStatus.*
import AlteredStatusUtility.*
import scalamon.logics.state.StatsStateModuleImpl.*

/**
 * Repository of all available moves in the game.
 *
 * This object acts as an in-memory database containing:
 * - Predefined moves grouped by type.
 * - Helper methods to filter and query moves.
 *
 * It is intended as a static reference dataset.
 */
object MoveDatabase:
  /**
   * Complete set of all defined moves in the game.
   *
   * Includes both damaging and non-damaging moves across all types.
   */
  val allMoves: Set[Move] = Set(
    // NORMAL
    move named "Body slam"
      withPower 85 withPP 24 withAccuracy 100 withType Normal
      withEffect (Effect applying Paralyzed withProbability 29.7)
      as Physical,
    move named "Hyper beam"
      withPower 150 withPP 8 withAccuracy 90 withType Normal
      withEffect (Effect recharging 1)
      as Special,
    move named "Double edge"
      withPower 100 withPP 24 withAccuracy 100 withType Normal
      withEffect (Effect recoil 25)
      as Physical,
    move named "Slash"
      withPower 70 withPP 32 withAccuracy 100 withType Normal
      withEffect (Effect multiplyingCriticalBy 8)
      as Physical,
    move named "Swift"
      withPower 60 withPP 32 withAccuracy 100 withType Normal
      as Special,
    move named "Strength"
      withPower 80 withPP 24 withAccuracy 100 withType Normal
      as Physical,
    move named "Recover"
      withPP 32 withAccuracy 100 withType Normal
      withEffect (Effect healing 50)
      as Status,

    // FIRE
    move named "Flamethrower"
      withPower 95 withPP 24 withAccuracy 100 withType Fire
      withEffect (Effect applying Burned withProbability 9.8)
      as Special,
    move named "Fire blast"
      withPower 120 withPP 8 withAccuracy 84 withType Fire
      withEffect (Effect applying Burned withProbability 9.8)
      as Special,
    move named "Fire punch"
      withPower 75 withPP 24 withAccuracy 100 withType Fire
      withEffect (Effect applying Burned withProbability 9.8)
      as Physical,
    move named "Ember"
      withPower 40 withPP 40 withAccuracy 100 withType Fire
      as Physical,
    move named "Fire spin"
      withPP 24 withAccuracy 70 withType Fire
      withEffect (Effect applying Burned withProbability 100)
      as Status,

    // WATER
    move named "Surf"
      withPower 95 withPP 24 withAccuracy 100 withType Water
      as Special,
    move named "Hydro pump"
      withPower 120 withPP 8 withAccuracy 80 withType Water
      as Special,
    move named "Bubble beam"
      withPower 60 withPP 32 withAccuracy 100 withType Water
      withEffect (Effect changing speed(decrease(1)) withProbability 9.8)
      as Special,
    move named "Crab hammer"
      withPower 90 withPP 16 withAccuracy 84 withType Water
      withEffect (Effect multiplyingCriticalBy 8)
      as Physical,

    // GRASS
    move named "Razor leaf"
      withPower 55 withPP 40 withAccuracy 95 withType Grass
      withEffect (Effect multiplyingCriticalBy 8)
      as Physical,
    move named "Solar beam"
      withPower 120 withPP 16 withAccuracy 100 withType Grass
      withEffect (Effect recharging 1)
      as Special,
    move named "Vine whip"
      withPower 35 withPP 40 withAccuracy 100 withType Grass
      as Physical,
    move named "Sleep powder"
      withPP 24 withAccuracy 75 withType Grass
      withEffect (Effect applying Sleeping(getSleepTurns) withProbability 100)
      as Status,

    // ELECTRIC
    move named "Thunderbolt"
      withPower 95 withPP 24 withAccuracy 100 withType Electric
      withEffect (Effect applying Paralyzed withProbability 9.8)
      as Special,
    move named "Thunder"
      withPower 120 withPP 16 withAccuracy 70 withType Electric
      withEffect (Effect applying Paralyzed withProbability 9.8)
      as Special,
    move named "Thunder wave"
      withPP 32 withAccuracy 90 withType Electric
      withEffect (Effect applying Paralyzed withProbability 99.6)
      as Status,
    move named "Thundershock"
      withPower 40 withPP 48 withAccuracy 100 withType Electric
      withEffect (Effect applying Paralyzed withProbability 9.8)
      as Special,

    // PSYCHIC
    move named "Psychic"
      withPower 90 withPP 16 withAccuracy 100 withType Psychic
      withEffect (Effect changing specialDefense(decrease(1)) withProbability 10)
      as Special,
    move named "Confusion"
      withPower 50 withPP 40 withAccuracy 100 withType Psychic
      withEffect (Effect applying Confused(getConfusionTurns) withProbability 9.8)
      as Special,
    move named "Psy beam"
      withPower 65 withPP 32 withAccuracy 100 withType Psychic
      withEffect (Effect applying Confused(getConfusionTurns) withProbability 9.8)
      as Special,
    move named "Freezing glare"
      withPower 90 withPP 10 withAccuracy 100 withType Psychic
      withEffect (Effect applying Frozen withProbability 10)
      as Special,
    move named "Hypnosis"
      withPP 32 withAccuracy 60 withType Psychic
      withEffect (Effect applying Sleeping(getSleepTurns) withProbability 100)
      as Status,

    // POISON
    move named "Sludge"
      withPower 65 withPP 32 withAccuracy 100 withType Poison
      withEffect (Effect applying Poisoned withProbability 29.7)
      as Special,
    move named "Poison sting"
      withPower 15 withPP 56 withAccuracy 100 withType Poison
      withEffect (Effect applying Poisoned withProbability 29.7)
      as Physical,
    move named "Acid"
      withPower 40 withPP 48 withAccuracy 100 withType Poison
      withEffect (Effect changing specialDefense(decrease(1)) withProbability 9.8)
      as Special,
    move named "Toxic"
      withPP 16 withAccuracy 90 withType Poison
      withEffect (Effect applying Poisoned withProbability 100)
      as Status
  )

  /**
   * Extracts all damage move (damaging moves).
   *
   * @return subset of allMoves containing only DamagingMoves instances.
   */
  def damagingMoves: Set[DamagingMove] =
    allMoves.collect({case m: DamagingMove => m})

  /**
   * Extracts all status move (non-damaging moves).
   *
   * @return subset of allMoves containing only NonDamagingMoves instances.
   */
  def nonDamagingMoves: Set[NonDamagingMove] =
    allMoves.collect({case m: NonDamagingMove => m})

  /**
   * Extension methods for querying collections of moves.
   *
   * Provides utility operations to filter and search moves
   * in a declarative and functional style.
   */
  extension (moves: Set[Move])

    /**
     * Filters moves by their elemental type.
     *
     * @param t elemental type to filter by.
     * @return subset of moves matching the given type.
     */
    infix def ofType(t: Type): Set[Move] = moves.filter(_.moveType == t)

    /**
     * Searches a move by its name (case-insensitive).
     *
     * @param name name of the move to search.
     * @return an optional Move if found.
     */
    infix def findByName(name: String): Option[Move] = moves.find(_.name.equalsIgnoreCase(name))

    /**
     * Filters damaging moves by their category (Physical or Special).
     *
     * @param category damage category to filter by.
     * @return subset of damaging moves matching the given category.
     */
    infix def ofCategory(category: DamageMoveCategory): Set[DamagingMove] =
      moves.collect({case m: DamagingMove if m.category == category => m})