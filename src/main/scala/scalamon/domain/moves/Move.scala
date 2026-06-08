package scalamon.domain.moves

import Accuracy.*
import Power.*
import PowerPoints.*
import scalamon.domain.types.Type

enum MoveCategory:
  case Physical,Special

sealed trait Move:
  def name: String
  def pp: PP
  def accuracy: Accuracy
  def moveType: Type

sealed trait DamagingMove extends Move:
  def power: Power
  def category: MoveCategory

case class StatusMove(
  name: String,
  pp: PP,
  accuracy: Accuracy,
  moveType: Type) extends Move

case class DamageMove(
  name: String,
  power: Power,
  pp: PP,
  accuracy: Accuracy,
  moveType: Type,
  category: MoveCategory) extends DamagingMove

// object Move per la factory, tipi opachi legati più strettamente alla mossa e logica comune