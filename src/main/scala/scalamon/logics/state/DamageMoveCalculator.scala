package scalamon.logics.state

trait DamageMoveCalculator:
  type BattleState
  type StatsState
  type Damage
  type Move

  def getDamage(state: BattleState, move: Move): Damage

object DamageMoveCalculatorImpl extends DamageMoveCalculator:

  import scalamon.domain.moves.DamageMoveCategory.*
  import scalamon.domain.types.*

  override type BattleState = BattleStateImpl.BattleState
  override type StatsState = StatsStateModuleImpl.StatsState
  override type Damage = Int
  override type Move = scalamon.domain.moves.DamageMove

  def getDamage(state: BattleState, move: Move): Damage =
    val attacker = state.self.getActive
    val defender = state.opponent.getActive

    // (((((2 * 50) / 5) + 2) * Power * (A / D)) / 50 + 2).floor * STAB * TypeEffectiveness * WhetherMultiplier

    val (atk, def_) = move.category.match
      case Physical => (attacker.modifiedStats.attack.toInt, defender.modifiedStats.defense.toInt)
      case Special => (attacker.modifiedStats.specialAttack.toInt, defender.modifiedStats.specialDefense.toInt)

    val power = move.power.asInt

    val baseFormula = (((2.0 * 50 / 5 + 2) * power * atk.toDouble / def_.toDouble) / 50 + 2).toInt

    val stab = if attacker.species.pokemonType == move.moveType then 1.5 else 1.0

    val typeEffectiveness = move.moveType.multiplierAgainst(defender.species.pokemonType)

    (baseFormula * stab * typeEffectiveness).toInt

    // MANCA IL WEATHER MULTIPLIER, MA NON E' ANCORA IMPLEMENTATO !!