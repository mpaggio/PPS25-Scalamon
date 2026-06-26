package scalamon.logics.state

import scalamon.domain.moves.DamageMove
import scalamon.domain.weather.Weather
import scalamon.domain.weather.Weather.ClearSky

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
  /** CANCELLA PURE IL COMMENTO QUANDO TI E' CHIARO PASO
   * Sono Flag persistenti a livello di battaglia
   * @param opponentSwitchBlocked se true l'avversario non può cambiare Pokemon (es Abilità Shadow Tag)
   * @param weatherSuppressed se true la condizione meteo non ha effetto (es Abilità Air Lock)
   * @param selfFlashFireActive se true l'abilità Flash Fire del Pokemon attivo del giocatore è attiva (es Abilità Flash Fire)
   */
  case class BattleFlags(
   opponentSwitchBlocked: Boolean = false,
   weatherSuppressed: Boolean = false,
   selfFlashFireActive: Boolean = false,
   lastOpponentMove: Option[DamageMove] = None
  )
  case class Bs(self: PlayerState, opponent: PlayerState, ambient: PassiveEffect, passiveEffects: List[PassiveEffect], weather: Weather = ClearSky, flags: BattleFlags = BattleFlags())
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
    infix def setWeather(w: Weather): BattleState = bs.copy(weather = w)
    infix def updateFlags(f: BattleFlags => BattleFlags): BattleState = bs.copy(flags = f(bs.flags))
