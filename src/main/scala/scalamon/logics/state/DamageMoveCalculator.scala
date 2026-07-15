package scalamon.logics.state

/**
 * This trait defines the interface for calculating the move's damage on the battle.
 */
trait DamageMoveCalculator:
  type BattleState
  type StatsState
  type Damage
  type Move

  def getDamage(state: BattleState, move: Move)(using DamagePolicy): Damage

object DamageMoveCalculatorImpl extends DamageMoveCalculator:

  import scalamon.domain.moves.DamageMoveCategory.*
  import scalamon.domain.types.*

  override type BattleState = BattleStateImpl.BattleState
  override type StatsState = StatsStateModuleImpl.StatsState
  override type Damage = Int
  override type Move = scalamon.domain.moves.DamageMove

  /**
   * Calculates the move's damage following the standard Pokémon damage formula.
   * @param state the current battle state
   * @param move the move being used by the attacking Pokémon
   * @param policy the difficulty level of the battle
   * @return the calculated damage as an integer
   */
  def getDamage(state: BattleState, move: Move)(using policy: DamagePolicy): Damage =
    val attacker = state.self.getActive
    val defender = state.opponent.getActive

    // (((((2 * 50) / 5) + 2) * Power * (A / D)) / 50 + 2).floor * STAB * TypeEffectiveness * WhetherMultiplier

    val (atk, def_) = move.category.match
      case Physical => (attacker.modifiedStats.attack, defender.modifiedStats.defense)
      case Special => (attacker.modifiedStats.specialAttack, defender.modifiedStats.specialDefense)

    val power = move.power.asInt

    val baseFormula = (((2.0 * 50 / 5 + 2) * power * atk.toDouble / def_.toDouble) / 50 + 2).toInt

    val stab = if attacker.species.pokemonType == move.moveType then 1.5 else 1.0

    val typeEffectiveness = move.moveType.multiplierAgainst(defender.species.pokemonType)

    (baseFormula * stab * typeEffectiveness * policy.multiplier).toInt

    // MANCA IL WEATHER MULTIPLIER, MA NON E' ANCORA IMPLEMENTATO !!