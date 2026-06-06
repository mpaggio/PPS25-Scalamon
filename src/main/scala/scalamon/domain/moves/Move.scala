package scalamon.domain.moves

import Accuracy.*
import Power.*

sealed trait Move:
  def name: String
  def power: Power
  def pp: Int
  def accuracy: Accuracy

case class DamageMove(name: String, power: Power, pp: Int, accuracy: Accuracy) extends Move

case class StatusMove(name: String, power: Power, pp: Int, accuracy: Accuracy) extends Move

// object Move per la factory, tipi opachi legati più strettamente alla mossa e logica comune