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

object TurnResolutionImpl extends TurnResolutionModule:

  override def isKnockedOut(pokemon: PokemonStateModuleImpl.Ps): Boolean =
    pokemon.currentHp.toInt <= 0

  override def isDefeated(player: PlayerStateModuleImpl.Ps): Boolean =
    player.team.values.forall(isKnockedOut)

  override def needsForcedSwitch(player: PlayerStateModuleImpl.Ps): Boolean =
    isKnockedOut(player.getActive) && !isDefeated(player)

  private def aliveBench(player: PlayerState): List[PokemonRef] =
    player.team.collect({
      case(id, pokemon) if id != player.activeId && !isKnockedOut(pokemon) => PokemonRef(id) }
    ).toList

  override def resolveTurn(state: BattleStateImpl.Bs): TurnResult = state match
    case state if isDefeated(state.opponent) => TurnResult.SelfWins(state)
    case state if isDefeated(state.self) => TurnResult.SelfLoses(state)
    case state if needsForcedSwitch(state.self) => TurnResult.ForcedSwitch(state, aliveBench(state.self))
    case state => TurnResult.Ongoing(state)

