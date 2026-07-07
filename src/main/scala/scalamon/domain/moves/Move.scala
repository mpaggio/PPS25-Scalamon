  package scalamon.domain.moves

import Accuracy.*
import Power.*
import PowerPoints.*
import scalamon.domain.types.Type

/**
 * Enumeration representing the status category.
 * Implemented as a different enumeration because of implementation needs.
 */
enum StatusMoveCategory:
  case Status

/**
 * Enumeration representing the damage move categories (Physical and Special)
 */
enum DamageMoveCategory:
  case Physical,Special

/**
 * Base abstraction for any move a Pokémon can perform in battle.
 *
 * A move is defined by:
 * - Name: unique identifier for the move.
 * - Power Points: the resource cost associated with the move execution.
 * - Accuracy: the probabilistic success rate.
 * - Type: the elemental affinity of the move.
 *
 * It is implemented as a sealed trait to ensure that the hierarchy
 * is closed, allowing for exhaustive pattern matching in the battle engine.
 */
sealed trait Move:
  /** @return The human-readable name of the move. */
  def name: String
  /** @return The power points defining maximum usage count. */
  def pp: PP
  /** @return The probability of the move hitting the target. */
  def accuracy: Accuracy
  /** @return The elemental type of the move. */
  def moveType: Type

/**
 * A specialized move that focuses on tactical changes rather than direct damage.
 * These moves are guaranteed to have a [[MoveEffect]] that transforms the battle state.
 */
sealed trait NonDamagingMove extends Move:
  /** @return The fixed Status category. */
  def category: StatusMoveCategory
  /** @return The mandatory tactical consequence of this move. */
  def effect: MoveEffect

/**
 * A specialized move that deals direct damage to a target.
 * It introduces offensive properties like power and optional secondary effect.
 */
sealed trait DamagingMove extends Move:
  /** @return The base damage value of the move. */
  def power: Power
  /** @return The offensive category of the move. */
  def category: DamageMoveCategory
  /** @return An optional secondary consequence applied upon a successful hit. */
  def effect: Option[MoveEffect]

/**
 * Concrete implementation of a move that applies purely tactical consequences.
 *
 * @param name The name of the move.
 * @param pp The initial power points of the move.
 * @param accuracy The hit probability of the move.
 * @param moveType The elemental type of the move.
 * @param category The category of the move.
 * @param effect The mandatory state transformation logic (effect).
 */
case class StatusMove(
  name: String,
  pp: PP,
  accuracy: Accuracy,
  moveType: Type,
  category: StatusMoveCategory,
  effect: MoveEffect) extends NonDamagingMove

  /**
   * Concrete implementation of a move that deals damage.
   *
   * @param name The name of the move.
   * @param power The base power of the move.
   * @param pp The initial power points of the move.
   * @param accuracy The hit probability of the move.
   * @param moveType The elemental type of the move.
   * @param category The category of the move.
   * @param effect The optional secondary tactical consequence (effect).
   */
case class DamageMove(
  name: String,
  power: Power,
  pp: PP,
  accuracy: Accuracy,
  moveType: Type,
  category: DamageMoveCategory,
  effect: Option[MoveEffect]) extends DamagingMove

/**
 * Module providing a fluent Domain Specific Language (DSL) for move creation.
 *
 * This DSL mitigates primitive obsession and opacity by providing readable verbs
 * that guide the construction of valid [[Move]] instances.
 */
object MoveDSL:
  import scalamon.domain.moves.StatusMoveCategory.*

  /**
   * Internal builder used by the DSL to accumulate properties before move instantiation.
   * Leverages optional fields and requirement validation at the terminal step.
   */
  case class MoveBuilder(
    name: Option[String] = None,
    power: Option[Power] = None,
    pp: Option[PP] = None,
    accuracy: Option[Accuracy] = None,
    moveType: Option[Type] = None,
    moveEffect: Option[MoveEffect] = None)

  /**
   * Entry point of the DSL.
   * @return A fresh [[MoveBuilder]].
   */
  def move: MoveBuilder =
    MoveBuilder()

  /**
   * Extension methods providing fluent DSL operators for building Move instances.
   */
  extension (builder: MoveBuilder)

    /**
     * Sets the move name.
     *
     * @param name name of the move.
     */
    infix def named(name: String): MoveBuilder =
      builder.copy(name = Some(name))

    /**
     * Sets the move base power.
     *
     * @param power power of the move expressed as a Double value.
     */
    infix def withPower(power: Double): MoveBuilder =
      builder.copy(power = Some(powerFromDouble(power)))

    /**
     * Sets the move base power.
     *
     * @param power power of the move expressed as an Int value.
     */
    infix def withPower(power: Int): MoveBuilder =
      builder.copy(power = Some(powerFromInt(power)))

    /**
     * Sets the move maximum power points.
     *
     * @param pp power points of the move expressed as Double.
     */
    infix def withPP(pp: Double): MoveBuilder =
      builder.copy(pp = Some(powerPointsFromDouble(pp)))

    /**
     * Sets the move maximum power points.
     *
     * @param pp power points of the move expressed as Int.
     */
    infix def withPP(pp: Int): MoveBuilder =
      builder.copy(pp = Some(powerPointsFromInt(pp)))

    /**
     * Sets the move hit accuracy.
     *
     * @param accuracy accuracy of the move expressed as ratio.
     */
    infix def withAccuracy(accuracy: Double): MoveBuilder =
      builder.copy(accuracy = Some(accuracyFromRatio(accuracy)))

    /**
     * Sets the move hit accuracy.
     *
     * @param accuracy accuracy of the move expressed as percentage.
     */
    infix def withAccuracy(accuracy: Int): MoveBuilder =
      builder.copy(accuracy = Some(accuracyFromPercent(accuracy)))

    /**
     * Sets the move elemental type.
     *
     * @param moveType type of the move.
     */
    infix def withType(moveType: Type): MoveBuilder =
      builder.copy(moveType = Some(moveType))

    /**
     * Sets the move effect.
     *
     * @param effect effect of the move.
     */
    infix def withEffect(effect: MoveEffect): MoveBuilder =
      builder.copy(moveEffect = Some(effect))

    /**
     * Terminal step for damaging moves.
     *
     * @param category category of the damage move.
     * @return A validated [[DamageMove]].
     * @throws IllegalArgumentException if mandatory fields are missing.
     */
    infix def as(category: DamageMoveCategory): DamageMove =
      require(
        builder.name.isDefined && builder.pp.isDefined &&
          builder.accuracy.isDefined && builder.moveType.isDefined, "Missing mandatory fields")
      require(builder.power.isDefined, "Power is required for damage moves")
      DamageMove(
        builder.name.get, builder.power.get,
        builder.pp.get, builder.accuracy.get,
        builder.moveType.get, category,
        builder.moveEffect)

    /**
     * Terminal step for non-damaging moves.
     *
     * @param category category of the status move.
     * @return A validated [[StatusMove]].
     * @throws IllegalArgumentException if mandatory fields are missing.
     */
    infix def as(category: StatusMoveCategory): StatusMove =
      require(
        builder.name.isDefined && builder.pp.isDefined &&
          builder.accuracy.isDefined && builder.moveType.isDefined, "Missing mandatory fields")
      require(builder.moveEffect.isDefined, "Status moves must have an effect")
      StatusMove(
        builder.name.get, builder.pp.get,
        builder.accuracy.get, builder.moveType.get,
        category, builder.moveEffect.get)