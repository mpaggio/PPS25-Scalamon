package scalamon.domain.moves

import Accuracy.*
import Power.*
import PowerPoints.*

sealed trait Move:
  def name: String
  def power: Power
  def pp: PP
  def accuracy: Accuracy

case class DamageMove(name: String, power: Power, pp: PP, accuracy: Accuracy) extends Move

case class StatusMove(name: String, power: Power, pp: PP, accuracy: Accuracy) extends Move

// object Move per la factory, tipi opachi legati più strettamente alla mossa e logica comune