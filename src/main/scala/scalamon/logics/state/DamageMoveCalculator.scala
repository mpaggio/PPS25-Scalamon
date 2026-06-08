package scalamon.logics.state

trait DamageMoveCalculator:
  type BattleState
  type StatsState
  type Damage
  type Move

  def getDamage(state: BattleState, move: Move): Damage


object DamageMoveCalculatorImpl extends DamageMoveCalculator:
  override type BattleState = BattleStateImpl.BattleState
  override type StatsState = StatsStateModuleImpl.StatsState
  override type Damage = Int
  override type Move = scalamon.domain.moves.DamageMove

  def getDamage(state: BattleState, move: Move): Damage =
    val a = state.user.getActive.modifiedStats.attack
    val d = state.enemy.getActive.modifiedStats.defense
    val stab = if (state.user.getActive.species.pokemonType == move.moveType) {
      1.5
    } else {
      1.0
    }
    return 1