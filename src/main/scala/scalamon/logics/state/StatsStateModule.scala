package scalamon.logics.state

import scalamon.domain.pokemon.statistics.{StatADT, Stats}

trait StatsStateModule:
  type StatsState
  type Stat
  type StatModifier = Stat => Stat

  def statState(value: Stats): StatsState

  extension (ss: StatsState)
    def attack(f: StatModifier): StatsState
    def defense(f: StatModifier): StatsState
    def specialAttack(f: StatModifier): StatsState
    def specialDefense(f: StatModifier): StatsState
    def speed(f: StatModifier): StatsState

object StatsStateModuleImpl extends StatsStateModule:
  override type StatsState = Stats
  override type Stat = StatADT.Stat

  def statState(value: Stats): StatsState = value

  extension (ss: StatsState)
    infix def attack(f: StatModifier): StatsState = ss.copy(attack = f(ss.attack))
    infix def defense(f: StatModifier): StatsState = ss.copy(defense = f(ss.defense))
    infix def specialAttack(f: StatModifier): StatsState = ss.copy(specialAttack = f(ss.specialAttack))
    infix def specialDefense(f: StatModifier): StatsState = ss.copy(specialDefense = f(ss.specialDefense))
    infix def speed(f: StatModifier): StatsState = ss.copy(speed = f(ss.speed))


object StatModule:
  import StatADT.*

  extension (s: Stat)
    infix def decrease(amount: Int): Stat = fromInt(s.toInt - amount)
    infix def increase(amount: Int): Stat = fromInt(s.toInt + amount)
    infix def multiply(factor: Double): Stat = fromInt((s.toInt * factor).toInt)