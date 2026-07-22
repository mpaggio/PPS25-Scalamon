package scalamon.domain.pokemon.statistics

import scalamon.domain.pokemon.statistics.StatADT.*

/**
 * Represents the base statistics of a Pokémon.
 * @param hp the base HP statistic
 * @param attack the base Attack statistic
 * @param defense the base Defense statistic
 * @param specialAttack the base Special Attack statistic
 * @param specialDefense the base Special Defense statistic
 * @param speed the base Speed statistic
 */
case class Stats(
  hp: Stat,
  attack: Stat,
  defense: Stat,
  specialAttack: Stat,
  specialDefense: Stat,
  speed: Stat
)