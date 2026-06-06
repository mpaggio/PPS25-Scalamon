package scalamon.domain.pokemon

import StatADT.Stat

case class Stats(
  hp: Stat,
  attack: Stat,
  defense: Stat,
  specialAttack: Stat,
  specialDefense: Stat,
  speed: Stat
)