package scalamon.logics.state

/**
 * A module that defines the state and operations for Pokémon stats.
 * It extends the StateComponent trait, providing a structure for managing
 * the state of a Pokémon's stats, including operations to modify them.
 */
trait StatsStateModule extends StateComponent:
  type StatsState
  type Stat
  override protected type State = StatsState
  override protected type InnerState = Stat
  type Stats

  /**
   * Creates the initial state of a Pokémon's stats given its base stats.
   */
  def statsInitialState(value: Stats): StatsState

  /**
   * Operations to modify individual stats. Each operation takes a function
   * that transforms the current stat value and returns a new stat value.
   */
  def maxHp(f: InnerOp): Op
  def attack(f: InnerOp): Op
  def defense(f: InnerOp): Op
  def specialAttack(f: InnerOp): Op
  def specialDefense(f: InnerOp): Op
  def speed(f: InnerOp): Op

  /**
   * Basic operations to modify a stat by decreasing, increasing, or multiplying it.
   */
  def decrease(amount: Int): InnerOp
  def increase(amount: Int): InnerOp
  def multiply(factor: Double): InnerOp

  extension (s: Stat)
    /** Clamps the stat value between a minimum and maximum value. */
    def clamped(min: Int, max: Int): Stat
    /** Ensures the stat value is non-negative. */
    def positive: Stat

/**
 * Concrete implementation of the StatsStateModule.
 * It defines the internal representation of the stats state and provides operations to manipulate it.
 * In addition, it exposes the parameters of the stats values as Int.
 */
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
