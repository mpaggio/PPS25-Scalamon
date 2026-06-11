package scalamon.logics.state

trait BattleStateModule extends StateComponent:
  type BattleState
  type PlayerState
  type PassiveEffect = (BattleState => BattleState) => List[BattleState => BattleState]
  override type SubComponent = PlayerState

  def battleState(enemyPokemon: PlayerState, userPokemon: PlayerState): BattleState
  extension (bs: BattleState)
    def self(f: Modifier): BattleState
    def opponent(f: Modifier): BattleState
    def switchUserEnemy: BattleState

object BattleStateImpl extends BattleStateModule:
  case class Bs(self: PlayerState, opponent: PlayerState, ambient: PassiveEffect, passiveEffects: List[PassiveEffect])
  override type BattleState = Bs
  override type PlayerState = PlayerStateModuleImpl.PlayerState
  def battleState(userPokemon: PlayerState, enemyPokemon: PlayerState): BattleState =
    Bs(userPokemon, enemyPokemon, bs => List(bs), List())

  extension (bs: BattleState)
    infix def self(f: Modifier): BattleState = bs.copy(self = f(bs.self), opponent = bs.opponent)
    infix def opponent(f: Modifier): BattleState = bs.copy(self = bs.self, opponent = f(bs.opponent))
    infix def switchUserEnemy: BattleState = bs.copy(self = bs.opponent, opponent = bs.self)
    infix def setAmbient(effect: PassiveEffect): BattleState = bs.copy(ambient = effect)
    infix def addPassiveEffect(effect: PassiveEffect): BattleState = 
      bs.copy(passiveEffects = effect :: bs.passiveEffects)
