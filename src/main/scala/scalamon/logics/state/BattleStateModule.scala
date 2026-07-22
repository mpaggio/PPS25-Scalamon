package scalamon.logics.state

/**
 * A module that defines the structure and operations for managing the state of a battle in a Pokémon-like game.
 * It extends the StateComponent trait, providing specific types and operations related to battle state management.
 * The two players are represented as `self` - the player who wants to modify the state - and `opponent` - the other player.
 */
trait BattleStateModule extends StateComponent:
  type BattleState
  protected type PlayerState
  override protected type State = BattleState
  override protected type InnerState = PlayerState
  type Weather
  type Trigger
  type Logger
  type PassiveEffect = Trigger => Op

  /**
   * Initializes the battle state with the given user and enemy Pokémon states and an initial weather condition.
   */
  def battleState(userPokemon: PlayerState, enemyPokemon: PlayerState, initialWeather: Weather): BattleState

  /**
   * Returns an operation that modifies the state of the player applying him the provided function `f`.
   */
  def self(f: InnerOp): Op

  /**
   * Returns an operation that modifies the state of the opponent applying him the provided function `f`.
   */
  def opponent(f: InnerOp): Op

  /**
   * Returns an operation that switches the states of the player and the opponent.
   * It is useful to change the perspective of the battle.
   */
  def switchSelfOpponent: Op

  /**
   * Returns an operation that adds a passive effect to the battle state.
   */
  def addPassiveEffect(key: String, effect: PassiveEffect): Op
  
  /**
   * Returns an operation that removes a passive effect from the battle state.
   * If the key does not exist, the operation has no effect.
   */
  def removePassiveEffect(key: String): Op

  /**
   * Returns an operation that sets the weather condition in the battle state.
   */
  def setWeather(w: Weather): Op

  /**
   * Returns an operation that updates the battle logs applying the provided function `f`.
   */
  def updateLogs(f: Logger => Logger): Op


/**
 * Concrete implementation of the BattleStateModule.
 * It defines the internal representation of the battle state and provides operations to manipulate it.
 * In addition, it exposes the parameters of the battle state to be read.
 */
object BattleStateModuleImpl extends BattleStateModule:
  
  import scalamon.logics.log.BattleLogger.*
  import scalamon.domain.weather.Weather.ClearSky

  case class Bs(
                 self: PlayerState,
                 opponent: PlayerState,
                 passiveEffects: Map[String, PassiveEffect],
                 weather: Weather,
                 logs: Logger
               )

  override type BattleState = Bs
  override type PlayerState = PlayerStateModuleImpl.PlayerState
  override type Weather = scalamon.domain.weather.Weather
  override type Trigger = scalamon.domain.pokemon.abilities.AbilityTrigger
  override type Logger = BattleLogger
  def battleState(userPokemon: PlayerState, enemyPokemon: PlayerState, initialWeather: Weather = ClearSky): BattleState =
    Bs(userPokemon, enemyPokemon, Map(), initialWeather, emptyLogger)

  def self(f: InnerOp): Op = bs => bs.copy(self = f(bs.self))
  def opponent(f: InnerOp): Op = bs => bs.copy(opponent = f(bs.opponent))
  def switchSelfOpponent: Op = bs => bs.copy(self = bs.opponent, opponent = bs.self)
  def addPassiveEffect(key: String, effect: PassiveEffect): Op = updatePassiveEffects(_ + (key -> effect))
  def removePassiveEffect(key: String): Op = updatePassiveEffects(_ - key)
  def setWeather(w: Weather): Op = bs => bs.copy(weather = w)
  def updateLogs(f: Logger => Logger): Op = bs => bs.copy(logs = f(bs.logs.setPlayer(bs.self.name) ))
  
  private def updatePassiveEffects(f: Map[String, PassiveEffect] => Map[String, PassiveEffect]): Op =
    bs => bs.copy(passiveEffects = f(bs.passiveEffects))
  