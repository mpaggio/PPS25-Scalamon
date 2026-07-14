package scalamon.logics.turns

import scalamon.domain.moves.AlteredStatus.*
import scalamon.domain.pokemon.abilities.MyAbilityBook
import scalamon.domain.pokemon.abilities.AbilityTrigger.OnTurnEnd
import scalamon.domain.moves.Accuracy.given
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.state.AlteredStatusModule.applyCondition
import scalamon.logics.weather.{WeatherEndTurnResolver, WeatherSystem}
import scalamon.logics.weather.WeatherSystem.default

/**
 * Identifies one of the two sides of the battle, relative to the current
 * orientation of the BattleState.
 *
 * NOTE: consider unifying this with `abilities.Target` if that enum only
 * contains Self/Opponent, to avoid two parallel concepts.
 */
enum Side:
  case Self, Opponent

/**
 * A pending mandatory switch for one side of the battle.
 */
final case class SwitchRequest(side: Side, candidates: List[PokemonRef])

/**
 * Outcome of a turn.
 *
 * The three previous cases ForcedSwitch / OpponentForcedSwitch /
 * BothForcedSwitch are collapsed into a single ForcedSwitch carrying the
 * list of pending requests (1 or 2 elements). SelfWins / SelfLoses are
 * collapsed into Victory(winner).
 */
enum TurnResult:
  case Ongoing
  case Victory(winner: Side)
  case ForcedSwitch(requests: List[SwitchRequest])

trait TurnResolutionModule:
  def isKnockedOut(pokemon: PokemonState): Boolean
  def isDefeated(player: PlayerState): Boolean
  def needsForcedSwitch(player: PlayerState): Boolean
  def resolveTurn(state: BattleState): (TurnResult, BattleState)
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
    player.team.collect {
      case (id, pokemon) if id != player.activeId && !isKnockedOut(pokemon) => PokemonRef(id)
    }.toList

  /** Projects the player of the given side out of the battle state. */
  private def playerOf(state: BattleState, side: Side): PlayerState = side match
    case Side.Self     => state.self
    case Side.Opponent => state.opponent

  /**
   * Resolves the outcome of a turn.
   *
   * Behaviourally equivalent to the previous 8-case pattern match:
   * - if self is defeated, the opponent wins (this also covers the "both
   *   defeated" tie, which previously resolved to SelfLoses);
   * - otherwise, if the opponent is defeated, self wins (the old
   *   "opponent defeated && self needs switch => SelfWins" case is
   *   subsumed, since a defeated opponent always means victory);
   * - otherwise every side whose active Pokémon is KO produces a
   *   SwitchRequest; zero requests means Ongoing.
   */
  override def resolveTurn(state: BattleState): (TurnResult, BattleState) =
    val stateAfterEnd = endTurn(state)
    val result =
      if isDefeated(stateAfterEnd.self) then TurnResult.Victory(Side.Opponent)
      else if isDefeated(stateAfterEnd.opponent) then TurnResult.Victory(Side.Self)
      else
        val requests = List(Side.Self, Side.Opponent).flatMap { side =>
          val player = playerOf(stateAfterEnd, side)
          Option.when(needsForcedSwitch(player))(SwitchRequest(side, aliveBench(player)))
        }
        if requests.isEmpty then TurnResult.Ongoing
        else TurnResult.ForcedSwitch(requests)
    (result, stateAfterEnd)

  override def applyForcedSwitch(player: PlayerState, newActive: PokemonRef): PlayerState =
    if player.team.contains(newActive.value) then switchActive(newActive.value)(player)
    else player

  /** Runs f from the perspective of the given side, restoring orientation. */
  private def onSide(side: Side)(f: BattleState => BattleState): BattleState => BattleState =
    side match
      case Side.Self     => f
      case Side.Opponent => s => switchSelfOpponent(f(switchSelfOpponent(s)))

  /** Runs f once per side, each time from that side's perspective. */
  private def forBothSides(f: BattleState => BattleState): BattleState => BattleState =
    state => List(Side.Self, Side.Opponent).foldLeft(state)((s, side) => onSide(side)(f)(s))

  private def applyStatusDamage: BattleState => BattleState =
    forBothSides { s =>
      s.self.getActive.status.foldLeft(s)((st, status) => status.applyCondition(st))
    }

  private def applyEndOfTurnAbilities: BattleState => BattleState =
    forBothSides { s =>
      MyAbilityBook.runTrigger(OnTurnEnd, s.self.getActive.species.abilitySlot)(s)
    }

  private def applyWeatherEffects(state: BattleState): BattleState =
    summon[WeatherEndTurnResolver].apply(state)

  override def endTurn(state: BattleState): BattleState =
    List[BattleState => BattleState](
      applyStatusDamage,
      applyWeatherEffects,
      applyEndOfTurnAbilities
    ).foldLeft(state)((s, f) => f(s))
