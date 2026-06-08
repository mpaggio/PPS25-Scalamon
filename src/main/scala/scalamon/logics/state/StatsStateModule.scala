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
  import scalamon.domain.pokemon.statistics
  import scalamon.domain.pokemon.statistics.StatADT.fromInt

  override type StatsState = statistics.Stats
  override type Stats = statistics.Stats
  override type Stat = statistics.StatADT.Stat

  def statsInitialState(value: Stats): StatsState = value

  extension (ss: StatsState)
    infix def attack(f: Modifier): StatsState = ss.copy(attack = f(ss.attack))
    infix def defense(f: Modifier): StatsState = ss.copy(defense = f(ss.defense))
    infix def specialAttack(f: Modifier): StatsState = ss.copy(specialAttack = f(ss.specialAttack))
    infix def specialDefense(f: Modifier): StatsState = ss.copy(specialDefense = f(ss.specialDefense))
    infix def speed(f: Modifier): StatsState = ss.copy(speed = f(ss.speed))

  extension (s: Stat)
    infix def decrease(amount: Int): Stat = fromInt(s.toInt - amount)
    infix def increase(amount: Int): Stat = fromInt(s.toInt + amount)
    infix def multiply(factor: Double): Stat = fromInt((s.toInt * factor).toInt)