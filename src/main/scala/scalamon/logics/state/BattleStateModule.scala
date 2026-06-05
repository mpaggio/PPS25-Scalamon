package scalamon.logics.state

trait StateModule:
  type InnerState
  type Function = InnerState => InnerState

trait BattleStateModule extends StateModule:
  type BattleState
  type PlayerState
  type PassiveEffect = BattleState => BattleState
  override type InnerState = PlayerState

  def battleState(enemyPokemon: PlayerState, userPokemon: PlayerState): BattleState
  extension (bs: BattleState)
    def user(f: Function): BattleState
    def enemy(f: Function): BattleState
    def switchUserEnemy: BattleState
    def ambient: PassiveEffect
    def passiveEffects: List[PassiveEffect]

object BattleStateImpl extends BattleStateModule:
  case class Bs(user: PlayerState, enemy: PlayerState, ambient: PassiveEffect, passiveEffects: List[PassiveEffect])
  override type BattleState = Bs
  override type PlayerState = PlayerStateModuleImpl.PlayerState
  def battleState(userPokemon: PlayerState, enemyPokemon: PlayerState): BattleState =
    Bs(userPokemon, enemyPokemon, identity, List())

  extension (bs: BattleState)
    infix def user(f: Function): BattleState = bs.copy(user = f(bs.user), enemy = bs.enemy)
    infix def enemy(f: Function): BattleState = bs.copy(user = bs.user, enemy = f(bs.enemy))
    def switchUserEnemy: BattleState = bs.copy(user = bs.enemy, enemy = bs.user)
    def ambient: PassiveEffect = bs.ambient
    def setAmbient(effect: PassiveEffect): BattleState = bs.copy(ambient = effect)
    def passiveEffects: List[PassiveEffect] = bs.passiveEffects
    def addPassiveEffect(effect: PassiveEffect): BattleState = 
      bs.copy(passiveEffects = effect :: bs.passiveEffects)