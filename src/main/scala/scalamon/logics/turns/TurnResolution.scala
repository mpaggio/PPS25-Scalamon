package scalamon.logics.turns

import scalamon.logics.state.BattleStateImpl.{BattleState, PlayerState}
import scalamon.logics.state.{BattleStateImpl, PlayerStateModuleImpl, PokemonStateModuleImpl}
import scalamon.logics.state.PokemonStateModuleImpl.PokemonState

enum TurnResult:
  case Ongoing(state: BattleState)
  case SelfWins(state: BattleState)
  case SelfLoses(state: BattleState)
  case ForcedSwitch(state: BattleState, candidates: List[PokemonRef])

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

  override def resolveTurn(state: BattleState): TurnResult = state match
    case state if isDefeated(state.opponent) => TurnResult.SelfWins(state)
    case state if isDefeated(state.self) => TurnResult.SelfLoses(state)
    case state if needsForcedSwitch(state.self) => TurnResult.ForcedSwitch(state, aliveBench(state.self))
    case state => TurnResult.Ongoing(state)

  override def applyForcedSwitch(player: PlayerState, newActive: PokemonRef): PlayerState =
    if player.team.contains(newActive.value) then player switchActive newActive.value
    else player

  override def endTurn(state: BattleState): BattleState = state
  /*  For now, we don't have any end-of-turn effects to apply, so this is a no-op.
      In the future, this is where we would handle things like status effects, weather damage,etc*/