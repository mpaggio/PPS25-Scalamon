package scalamon.domain.pokemon

case class Stats(
  hp: Int,
  attack: Int,
  defense: Int,
  specialAttack: Int,
  specialDefense: Int,
  speed: Int
):
  require(hp > 0, "HP must be a value greater than 0")
  require(attack > 0, "Attack must be a value greater than 0")
  require(defense > 0, "Defense must be a value greater than 0")
  require(specialAttack > 0, "Special Attack must be a value greater than 0")
  require(specialDefense > 0, "Special Defense must be a value greater than 0")
  require(speed > 0, "Speed must be a value greater than 0")