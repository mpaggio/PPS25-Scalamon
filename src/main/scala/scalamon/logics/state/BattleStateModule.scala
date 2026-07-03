package scalamon.logics.state

import scalamon.domain.moves.DamageMove
import scalamon.domain.weather.Weather
import scalamon.domain.weather.Weather.ClearSky

trait BattleStateModule extends StateComponent:
  type BattleState
  protected type PlayerState
  override protected type State = BattleState
  override protected type InnerState = PlayerState
  type Trigger
  type PassiveEffect = Trigger => Op

  def battleState(enemyPokemon: PlayerState, userPokemon: PlayerState): BattleState
  
  def self(f: InnerOp): Op
  def opponent(f: InnerOp): Op
  def switchSelfOpponent: Op

object BattleStateImpl extends BattleStateModule:
  
  case class BattleFlags(
   opponentSwitchBlocked: Boolean = false,
   weatherSuppressed: Boolean = false,
   selfFlashFireActive: Boolean = false,
   selfMagicGuardActive: Boolean = false,
   lastOpponentMove: Option[DamageMove] = None
  )

  
  case class Bs(self: PlayerState, opponent: PlayerState, passiveEffects: List[PassiveEffect], weather: Weather, flags: BattleFlags)
  override type BattleState = Bs
  override type PlayerState = PlayerStateModuleImpl.PlayerState
  override type Trigger = scalamon.domain.pokemon.abilities.AbilityTrigger
  def battleState(userPokemon: PlayerState, enemyPokemon: PlayerState): BattleState =
    Bs(userPokemon, enemyPokemon, List(), ClearSky, BattleFlags())

  case class opponentOp(f: InnerOp):
    def apply: Op = bs => bs.copy(self = bs.self, opponent = f(bs.opponent))

  def self(f: InnerOp): Op = bs => bs.copy(self = f(bs.self))
  def opponent(f: InnerOp): Op = bs => bs.copy(opponent = f(bs.opponent))
  def switchSelfOpponent: Op = bs => bs.copy(self = bs.opponent, opponent = bs.self)
  def addPassiveEffect(effect: PassiveEffect): Op = bs => bs.copy(passiveEffects = effect :: bs.passiveEffects)
  def setWeather(w: Weather): Op = bs => bs.copy(weather = w)
  
  extension (bs: BattleState)
    def updateFlags(f: BattleFlags => BattleFlags): BattleState = bs.copy(flags = f(bs.flags))