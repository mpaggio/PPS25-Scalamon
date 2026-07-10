package scalamon.logics.state

trait BattleStateModule extends StateComponent:
  type BattleState
  protected type PlayerState
  override protected type State = BattleState
  override protected type InnerState = PlayerState
  type Weather
  type Trigger
  type Logger
  type PassiveEffect = Trigger => Op

  def battleState(enemyPokemon: PlayerState, userPokemon: PlayerState, initialWeather: Weather): BattleState
  
  def self(f: InnerOp): Op
  def opponent(f: InnerOp): Op
  def switchSelfOpponent: Op
  def addPassiveEffect(effect: PassiveEffect): Op
  def setWeather(w: Weather): Op
  def updateLogs(f: Logger => Logger): Op

object BattleStateImpl extends BattleStateModule:
  
  import scalamon.logics.log.BattleLogger.*
  import scalamon.domain.moves.DamageMove
  import scalamon.domain.weather.Weather.ClearSky
  
  case class BattleFlags(
   opponentSwitchBlocked: Boolean = false,
   weatherSuppressed: Boolean = false,
   selfFlashFireActive: Boolean = false,
   selfMagicGuardActive: Boolean = false,
   lastOpponentMove: Option[DamageMove] = None
  )

  case class Bs(
                 self: PlayerState,
                 opponent: PlayerState,
                 passiveEffects: List[PassiveEffect],
                 weather: Weather,
                 flags: BattleFlags,
                 logs: Logger
               )

  override type BattleState = Bs
  override type PlayerState = PlayerStateModuleImpl.PlayerState
  override type Weather = scalamon.domain.weather.Weather
  override type Trigger = scalamon.domain.pokemon.abilities.AbilityTrigger
  override type Logger = BattleLogger
  def battleState(userPokemon: PlayerState, enemyPokemon: PlayerState, initialWeather: Weather = ClearSky): BattleState =
    Bs(userPokemon, enemyPokemon, List(), initialWeather, BattleFlags(), emptyLogger)

  case class opponentOp(f: InnerOp):
    def apply: Op = bs => bs.copy(self = bs.self, opponent = f(bs.opponent))

  def self(f: InnerOp): Op = bs => bs.copy(self = f(bs.self))
  def opponent(f: InnerOp): Op = bs => bs.copy(opponent = f(bs.opponent))
  def switchSelfOpponent: Op = bs => bs.copy(self = bs.opponent, opponent = bs.self)
  def addPassiveEffect(effect: PassiveEffect): Op = bs => bs.copy(passiveEffects = effect :: bs.passiveEffects)
  def setWeather(w: Weather): Op = bs => bs.copy(weather = w)
  def updateLogs(f: Logger => Logger): Op = bs => bs.copy(logs = f(bs.logs.setPlayer(bs.self.name) ))
  
  extension (bs: BattleState)
    def updateFlags(f: BattleFlags => BattleFlags): BattleState = bs.copy(flags = f(bs.flags))