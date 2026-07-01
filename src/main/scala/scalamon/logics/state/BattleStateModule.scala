package scalamon.logics.state

import scalamon.domain.moves.DamageMove
import scalamon.domain.weather.Weather
import scalamon.domain.weather.Weather.ClearSky

trait BattleStateModule extends StateComponent:
  type BattleState
  protected type PlayerState
  override protected type State = BattleState
  override protected type InnerState = PlayerState
  type PassiveEffect = (BattleState => BattleState) => List[BattleState => BattleState]

  def battleState(enemyPokemon: PlayerState, userPokemon: PlayerState): BattleState
  
  def identity: Op
  def self(f: InnerOp): Op
  def opponent(f: InnerOp): Op
  def switchSelfOpponent: Op

object BattleStateImpl extends BattleStateModule:
  /** CANCELLA PURE IL COMMENTO QUANDO TI E' CHIARO PASO
   * Sono Flag persistenti a livello di battaglia
   * @param opponentSwitchBlocked se true l'avversario non può cambiare Pokemon (es Abilità Shadow Tag)
   * @param weatherSuppressed se true la condizione meteo non ha effetto (es Abilità Air Lock)
   * @param selfFlashFireActive se true l'abilità Flash Fire del Pokemon attivo del giocatore è attiva (es Abilità Flash Fire)
    * @param selfMagicGuardActive se true l'abilità Magic Guard del Pokemon attivo del giocatore è attiva (es Abilità Magic Guard)
   */
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
  def battleState(userPokemon: PlayerState, enemyPokemon: PlayerState): BattleState =
    Bs(userPokemon, enemyPokemon, List(), ClearSky, BattleFlags())

  case class opponentOp(f: InnerOp):
    def apply: Op = bs => bs.copy(self = bs.self, opponent = f(bs.opponent))

  def identity: Op = bs => bs
  def self(f: InnerOp): Op = bs => bs.copy(self = f(bs.self))
  def opponent(f: InnerOp): Op = bs => bs.copy(opponent = f(bs.opponent))
  def switchSelfOpponent: Op = bs => bs.copy(self = bs.opponent, opponent = bs.self)
  def addPassiveEffect(effect: PassiveEffect): Op = bs => bs.copy(passiveEffects = effect :: bs.passiveEffects)
  def setWeather(w: Weather): Op = bs => bs.copy(weather = w)
  
  extension (bs: BattleState)
    def updateFlags(f: BattleFlags => BattleFlags): BattleState = bs.copy(flags = f(bs.flags))