package scalamon.domain.moves

import Accuracy.*
import Power.*
import PowerPoints.*
import scalamon.domain.types.Type

enum StatusMoveCategory:
  case Status

enum DamageMoveCategory:
  case Physical,Special

sealed trait Move:
  def name: String
  def pp: PP
  def accuracy: Accuracy
  def moveType: Type

sealed trait NonDamagingMove extends Move:
  def category: StatusMoveCategory

sealed trait DamagingMove extends Move:
  def power: Power
  def category: DamageMoveCategory

case class StatusMove(
  name: String,
  pp: PP,
  accuracy: Accuracy,
  moveType: Type,
  category: StatusMoveCategory) extends NonDamagingMove

case class DamageMove(
  name: String,
  power: Power,
  pp: PP,
  accuracy: Accuracy,
  moveType: Type,
  category: DamageMoveCategory) extends DamagingMove

object MoveDSL:
  import scalamon.domain.moves.StatusMoveCategory.*

  case class MoveBuilder(
    name: Option[String] = None,
    power: Option[Power] = None,
    pp: Option[PP] = None,
    accuracy: Option[Accuracy] = None,
    moveType: Option[Type] = None)

  def move: MoveBuilder =
    MoveBuilder()

  extension (builder: MoveBuilder)

    infix def named(name: String): MoveBuilder =
      builder.copy(name = Some(name))

    infix def withPower(power: Double): MoveBuilder =
      builder.copy(power = Some(powerFromDouble(power)))

    infix def withPower(power: Int): MoveBuilder =
      builder.copy(power = Some(powerFromInt(power)))

    infix def withPP(pp: Double): MoveBuilder =
      builder.copy(pp = Some(powerPointsFromDouble(pp)))

    infix def withPP(pp: Int): MoveBuilder =
      builder.copy(pp = Some(powerPointsFromInt(pp)))

    infix def withAccuracy(accuracy: Double): MoveBuilder =
      builder.copy(accuracy = Some(accuracyFromRatio(accuracy)))

    infix def withAccuracy(accuracy: Int): MoveBuilder =
      builder.copy(accuracy = Some(accuracyFromPercent(accuracy)))

    infix def withType(moveType: Type): MoveBuilder =
      builder.copy(moveType = Some(moveType))

    infix def as(category: DamageMoveCategory): DamageMove =
      buildDamaging(category)

    infix def as(category: StatusMoveCategory): StatusMove =
      buildStatus(category)

    private def buildStatus(category: StatusMoveCategory): StatusMove =
      require(
        builder.name.isDefined && builder.pp.isDefined &&
          builder.accuracy.isDefined && builder.moveType.isDefined)
      StatusMove(
        builder.name.get, builder.pp.get,
        builder.accuracy.get, builder.moveType.get, category)

    private def buildDamaging(category: DamageMoveCategory): DamageMove =
      val base = buildStatus(Status)
      require(builder.power.isDefined && builder.moveType.isDefined)
      DamageMove(
        base.name, builder.power.get,
        base.pp, base.accuracy,
        base.moveType, category)



