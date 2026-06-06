package scalamon.domain.moves

import Accuracy.*

sealed trait Move:
  def name: String
  def power: Int
  def pp: Int
  def accuracy: Accuracy

case class DamageMove(name: String, power: Int, pp: Int, accuracy: Accuracy) extends Move

case class StatusMove(name: String, power: Int, pp: Int, accuracy: Accuracy) extends Move

// object Move per la factory, tipi opachi legati più strettamente alla mossa e logica comune