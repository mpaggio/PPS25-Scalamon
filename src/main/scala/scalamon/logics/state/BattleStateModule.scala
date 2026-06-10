package scalamon.logics.state

trait BattleStateModule extends StateComponent:
  type BattleState
  type PlayerState
  type PassiveEffect = (BattleState => BattleState) => List[BattleState => BattleState]
  override type SubComponent = PlayerState

  def battleState(enemyPokemon: PlayerState, userPokemon: PlayerState): BattleState
  extension (bs: BattleState)
    def user(f: Modifier): BattleState
    def enemy(f: Modifier): BattleState
    def switchUserEnemy: BattleState

object BattleStateImpl extends BattleStateModule:
  case class Bs(user: PlayerState, enemy: PlayerState, ambient: PassiveEffect, passiveEffects: List[PassiveEffect])
  override type BattleState = Bs
  override type PlayerState = PlayerStateModuleImpl.PlayerState
  def battleState(userPokemon: PlayerState, enemyPokemon: PlayerState): BattleState =
    Bs(userPokemon, enemyPokemon, bs => List(bs), List())

  extension (bs: BattleState)
    infix def user(f: Modifier): BattleState = bs.copy(user = f(bs.user), enemy = bs.enemy)
    infix def enemy(f: Modifier): BattleState = bs.copy(user = bs.user, enemy = f(bs.enemy))
    infix def switchUserEnemy: BattleState = bs.copy(user = bs.enemy, enemy = bs.user)
    infix def setAmbient(effect: PassiveEffect): BattleState = bs.copy(ambient = effect)
    infix def addPassiveEffect(effect: PassiveEffect): BattleState = 
      bs.copy(passiveEffects = effect :: bs.passiveEffects)
