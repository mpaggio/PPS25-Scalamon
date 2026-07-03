package scalamon.logics.state

trait StatsStateModule extends StateComponent:
  type StatsState
  type Stat
  override protected type State = StatsState
  override protected type InnerState = Stat
  type Stats

  def statsInitialState(value: Stats): StatsState

  def attack(f: InnerOp): Op
  def defense(f: InnerOp): Op
  def specialAttack(f: InnerOp): Op
  def specialDefense(f: InnerOp): Op
  def speed(f: InnerOp): Op

  def decrease(amount: Int): InnerOp
  def increase(amount: Int): InnerOp
  def multiply(factor: Double): InnerOp

  extension (s: Stat)
    def clamped(min: Int, max: Int): Stat
    def positive: Stat

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

  def maxHp(f: InnerOp): Op = ss => ss.copy(hp = f(ss.hp))
  def attack(f: InnerOp): Op = ss => ss.copy(attack = f(ss.attack))
  def defense(f: InnerOp): Op = ss => ss.copy(defense = f(ss.defense))
  def specialAttack(f: InnerOp): Op = ss => ss.copy(specialAttack = f(ss.specialAttack))
  def specialDefense(f: InnerOp): Op = ss => ss.copy(specialDefense = f(ss.specialDefense))
  def speed(f: InnerOp): Op = ss => ss.copy(speed = f(ss.speed))

  def decrease(amount: Int): Stat => Stat = _ - amount
  def increase(amount: Int): Stat => Stat = _ + amount
  def multiply(factor: Double): Stat => Stat = s => (s * factor).toInt

  extension (s: Stat)
    def clamped(min: Int, max: Int): Stat = s.max(min).min(max)
    def positive: Stat = s.max(0)
