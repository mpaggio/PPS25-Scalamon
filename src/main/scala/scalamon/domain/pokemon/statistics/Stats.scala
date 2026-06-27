package scalamon.domain.pokemon.statistics

import scalamon.domain.pokemon.statistics.StatADT.*

case class Stats(
  hp: Stat,
  attack: Stat,
  defense: Stat,
  specialAttack: Stat,
  specialDefense: Stat,
  speed: Stat
)