package scalamon.logics.state

trait StatsStateModule extends StateComponent:
  type StatsState
  type Stats
  type Stat
  override type SubComponent = Stat

  def statsInitialState(value: Stats): StatsState

  extension (ss: StatsState)
    def attack(f: Modifier): StatsState
    def defense(f: Modifier): StatsState
    def specialAttack(f: Modifier): StatsState
    def specialDefense(f: Modifier): StatsState
    def speed(f: Modifier): StatsState

  extension (s: Stat)
    infix def decrease(amount: Int): Stat
    infix def increase(amount: Int): Stat
    infix def multiply(factor: Double): Stat


object StatsStateModuleImpl extends StatsStateModule:

  case class Ss(hp: Stat, attack: Stat, defense: Stat, specialAttack: Stat, specialDefense: Stat, speed: Stat)
  override type StatsState = Ss
  override type Stats = scalamon.domain.pokemon.statistics.Stats
  override type Stat = Int
  def statsInitialState(value: Stats): StatsState = Ss(
    value.hp.toInt,
    value.attack.toInt,
    value.defense.toInt,
    value.specialAttack.toInt,
    value.specialDefense.toInt,
    value.speed.toInt
  )

  extension (ss: StatsState)
    infix def maxHp(f: Modifier): StatsState = ss.copy(hp = f(ss.hp))
    infix def attack(f: Modifier): StatsState = ss.copy(attack = f(ss.attack))
    infix def defense(f: Modifier): StatsState = ss.copy(defense = f(ss.defense))
    infix def specialAttack(f: Modifier): StatsState = ss.copy(specialAttack = f(ss.specialAttack))
    infix def specialDefense(f: Modifier): StatsState = ss.copy(specialDefense = f(ss.specialDefense))
    infix def speed(f: Modifier): StatsState = ss.copy(speed = f(ss.speed))

  extension (s: Stat)
    infix def decrease(amount: Int): Stat = s - amount
    infix def increase(amount: Int): Stat = s + amount
    infix def multiply(factor: Double): Stat = (s * factor).toInt

    infix def clamped(min: Int, max: Int): Stat = s.max(min).min(max)
    infix def positive: Stat = s.max(0)
