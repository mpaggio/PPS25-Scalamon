package scalamon.logics.turns

import scalamon.domain.moves.AlteredStatus.*
import scalamon.domain.pokemon.abilities
import scalamon.domain.pokemon.abilities.MyAbilityBook
import scalamon.domain.pokemon.abilities.AbilityTrigger.OnTurnEnd
import scalamon.domain.moves.Accuracy.given
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.state.AlteredStatusModule.applyCondition
import scalamon.logics.weather.{WeatherEndTurnResolver, WeatherSystem}
import scalamon.logics.weather.WeatherSystem.default

import scala.language.postfixOps

/**
 * Determines the outcome of a turn based on the current state of the battle,
 * and applies any necessary end-of-turn effects.
 */
enum TurnResult:
  case Ongoing(state: BattleState)
  case SelfWins(state: BattleState)
  case SelfLoses(state: BattleState)
  case ForcedSwitch(state: BattleState, candidates: List[PokemonRef])
  case OpponentForcedSwitch(state: BattleState, candidates: List[PokemonRef])
  case BothForcedSwitch(state: BattleState, selfCandidates: List[PokemonRef], opponentCandidates: List[PokemonRef])

/**
 * Defines the interface for turn resolution logic, including methods to check for knockouts,
 * determine if a player is defeated, and resolve the outcome of a turn.
 * It also includes methods to apply forced switches and handle end-of-turn effects.
 */
trait TurnResolutionModule:
  def isKnockedOut(pokemon: PokemonState): Boolean
  def isDefeated(player: PlayerState): Boolean
  def needsForcedSwitch(player: PlayerState): Boolean
  def resolveTurn(state: BattleState): TurnResult
  def applyForcedSwitch(player: PlayerState, newActive: PokemonRef): PlayerState
  def endTurn(state: BattleState): BattleState

object TurnResolutionImpl extends TurnResolutionModule:

  override def isKnockedOut(pokemon: PokemonState): Boolean =
    pokemon.currentHp <= 0

  override def isDefeated(player: PlayerState): Boolean =
    player.team.values.forall(isKnockedOut)

  override def needsForcedSwitch(player: PlayerState): Boolean =
    isKnockedOut(player.getActive) && !isDefeated(player)

  private def aliveBench(player: PlayerState): List[PokemonRef] =
    player.team.collect({
      case(id, pokemon) if id != player.activeId && !isKnockedOut(pokemon) => PokemonRef(id) }
    ).toList

  /**
   * Resolves the outcome of a turn based on the current state of the battle.
   * It checks for various conditions such as knockouts and forced switches.
   * @param state The current state of the battle after all actions have been executed.
   * @return  the result of the turn, which can be of the types described in the TurnResult enum.
   */
  override def resolveTurn(state: BattleState): TurnResult = state match
    case state if isDefeated(state.self) && isDefeated(state.opponent) => TurnResult.SelfLoses(state)
    case state if isDefeated(state.self) && needsForcedSwitch(state.opponent)    => TurnResult.SelfLoses(state)
    case state if isDefeated(state.opponent) && needsForcedSwitch(state.self)    => TurnResult.SelfWins(state)
    case state if needsForcedSwitch(state.self) && needsForcedSwitch(state.opponent) => TurnResult.BothForcedSwitch(state, aliveBench(state.self), aliveBench(state.opponent))
    case state if needsForcedSwitch(state.self) => TurnResult.ForcedSwitch(state, aliveBench(state.self))
    case state if needsForcedSwitch(state.opponent) => TurnResult.OpponentForcedSwitch(state, aliveBench(state.opponent))
    case state if isDefeated(state.opponent) => TurnResult.SelfWins(state)
    case state if isDefeated(state.self) => TurnResult.SelfLoses(state)
    case state => TurnResult.Ongoing(endTurn(state))

  override def applyForcedSwitch(player: PlayerState, newActive: PokemonRef): PlayerState =
    if player.team.contains(newActive.value) then switchActive(newActive.value)(player)
    else player

  /**
   * Applies status damage to both players' active Pokémon at the end of the turn (if necessary).
   * @param state The current state of the battle.
   * @return The updated state of the battle after applying status damage to both players' active Pokémon.
   */
  private def applyStatusDamage(state: BattleState) =
    def applyForPlayer(s: BattleState, isSelf: Boolean): BattleState =
      val oriented = if isSelf then s else switchSelfOpponent(s)
      val pokemon = oriented.self.getActive
      val updated = pokemon.status.foldLeft(oriented)((state, status) => status.applyCondition(state))
      if isSelf then updated else switchSelfOpponent(updated)
    applyForPlayer(applyForPlayer(state, true), false)

  /**
   * Applies end-of-turn abilities for both players' active Pokémon (if necessary).
   * @param state The current state of the battle.
   * @return The updated state of the battle after applying end-of-turn abilities for both players' active Pokémon.
   */
  private def applyEndOfTurnAbilities(state: BattleState) =
    def applyForPlayer(s: BattleState, isSelf: Boolean): BattleState =
      val slot = if isSelf then s.self.getActive.species.abilitySlot
      else s.opponent.getActive.species.abilitySlot
      val oriented = if isSelf then s else switchSelfOpponent(s)
      val result = MyAbilityBook.runTrigger(OnTurnEnd, slot)(oriented)
      if isSelf then result else switchSelfOpponent(result)
    applyForPlayer(applyForPlayer(state, true), false)

  /**
   * Applies weather effects to the battle state at the end of the turn.
   * @param state The current state of the battle.
   * @return the updated state of the battle after applying weather effects.
   */
  private def applyWeatherEffects(state: BattleState): BattleState =
    summon[WeatherEndTurnResolver].apply(state)

  /**
   * Resolves the end-of-turn effects for the battle, including status damage, weather effects, and end-of-turn abilities.
   * @param state the current state of the battle after all actions have been executed.
   * @return the updated state of the battle after applying all end-of-turn effects.
   */
  override def endTurn(state: BattleState): BattleState =
    List[BattleState => BattleState](
      applyStatusDamage,
      applyWeatherEffects,
      applyEndOfTurnAbilities
    ).foldLeft(state)((s, f) => f(s))
