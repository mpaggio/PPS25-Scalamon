package scalamon.domain.pokemon.statistics

import scalamon.domain.pokemon.statistics.StatADT.*
import scalamon.domain.pokemon.statistics.StatADT.StatKind.*

case class Stats(
  hp: Stat,
  attack: Stat,
  defense: Stat,
  specialAttack: Stat,
  specialDefense: Stat,
  speed: Stat
)

extension (stats: Stats)
  def get(kind: StatKind): Stat = kind match
    case Hp => stats.hp
    case Attack => stats.attack
    case Defense => stats.defense
    case SpecialAttack => stats.specialAttack
    case SpecialDefense => stats.specialDefense
    case Speed => stats.speed

  def set(kind: StatKind, value: Stat): Stats = kind match
    case Hp => stats.copy(hp = value)
    case Attack => stats.copy(attack = value)
    case Defense => stats.copy(defense = value)
    case SpecialAttack => stats.copy(specialAttack = value)
    case SpecialDefense => stats.copy(specialDefense = value)
    case Speed => stats.copy(speed = value)