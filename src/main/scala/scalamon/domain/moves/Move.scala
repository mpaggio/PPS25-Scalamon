package scalamon.domain.moves

import Accuracy.*

trait Move:
  def name: String
  def accuracy: Accuracy

case class DamageMove(name: String, accuracy: Accuracy) extends Move