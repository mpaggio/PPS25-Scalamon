package scalamon.domain.moves

import Accuracy.*
import Power.*
import PowerPoints.*
import scalamon.domain.types.Type

enum StatusMoveCategory:
  case Status

enum DamageMoveCategory:
  case Physical,Special

/**
 * Base trait representing a Pokemon move.
 *
 * A move is defined by:
 * - A name.
 * - A number of power points (PP).
 * - An accuracy value.
 * - A move type (e.g. Fire, Water, Electric).
 *
 * This is the common abstraction for both damaging and non-damaging moves.
 */
sealed trait Move:
  def name: String
  def pp: PP
  def accuracy: Accuracy
  def moveType: Type

/**
 * Represents a move that does not deal direct damage.
 *
 * These moves are typically status-oriented and apply effects
 * such as healing, stat changes or status conditions.
 */
sealed trait NonDamagingMove extends Move:
  def category: StatusMoveCategory
  def effect: MoveEffect

/**
 * Represents a move that deals direct damage.
 * Power represents the amount of damage.
 *
 * These moves may optionally have a secondary effect
 * such as status inflictions or stat changes.
 */
sealed trait DamagingMove extends Move:
  def power: Power
  def category: DamageMoveCategory
  def effect: Option[MoveEffect]

/**
 * Concrete implementation of non-damaging move.
 * Always includes a mandatory MoveEffect.
 */
case class StatusMove(
  name: String,
  pp: PP,
  accuracy: Accuracy,
  moveType: Type,
  category: StatusMoveCategory,
  effect: MoveEffect) extends NonDamagingMove

/**
 * Concrete implementation of damaging move.
 * The effect is optional and may be None.
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
 * Domain Specific Language (DSL) for constructing Move instances.
 *
 * This DSL provides a fluent API to define secondary move effects
 * in a readable and expressive way, avoiding direct use of constructors.
 *
 * Example usage:
 * {{{
 * move named "Thunder" withPower 110 withPP 10 withAccuracy 70 withType Electric withEffect (Effect applying Paralyzed withProbability 10) as Special
 * }}}
 */
object MoveDSL:
  import scalamon.domain.moves.StatusMoveCategory.*

  /**
   * Internal builder used by the DSL to accumulate move properties.
   *
   * All fields are optional during construction and validated
   * when converting into a concrete Move instance.
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
   */
  def move: MoveBuilder =
    MoveBuilder()

  /**
   * Extension methods providing fluent DSL operators
   * for building Move instances.
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
     * Sets the move name.
     *
     * @param power power of the move expressed as a Double value.
     */
    infix def withPower(power: Double): MoveBuilder =
      builder.copy(power = Some(powerFromDouble(power)))

    /**
     * Sets the move name.
     *
     * @param power power of the move expressed as an Int value.
     */
    infix def withPower(power: Int): MoveBuilder =
      builder.copy(power = Some(powerFromInt(power)))

    /**
     * Sets the move power points.
     *
     * @param pp power points of the move expressed as Double.
     */
    infix def withPP(pp: Double): MoveBuilder =
      builder.copy(pp = Some(powerPointsFromDouble(pp)))

    /**
     * Sets the move power points.
     *
     * @param pp power points of the move expressed as Int.
     */
    infix def withPP(pp: Int): MoveBuilder =
      builder.copy(pp = Some(powerPointsFromInt(pp)))

    /**
     * Sets the move accuracy.
     *
     * @param accuracy accuracy of the move expressed as ratio.
     */
    infix def withAccuracy(accuracy: Double): MoveBuilder =
      builder.copy(accuracy = Some(accuracyFromRatio(accuracy)))

    /**
     * Sets the move accuracy.
     *
     * @param accuracy accuracy of the move expressed as percentage.
     */
    infix def withAccuracy(accuracy: Int): MoveBuilder =
      builder.copy(accuracy = Some(accuracyFromPercent(accuracy)))

    /**
     * Sets the move type.
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
     * Finalizes the builder into a DamageMove.
     * Requires name, pp, accuracy, type and power.
     *
     * @param category category of the damage move.
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
     * Finalizes the builder into a StatusMove.
     * Requires name, pp, accuracy, type and effect.
     *
     * @param category category of the status move.
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