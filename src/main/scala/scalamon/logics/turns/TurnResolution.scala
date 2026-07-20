package scalamon.logics.turns

import scalamon.domain.alteredStatus.AlteredStatus.*
import scalamon.domain.pokemon.abilities.AbilityTrigger.OnTurnEnd
import scalamon.domain.moves.Accuracy.given
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.domain.alteredStatus.AlteredStatusModule.*
import scalamon.logics.weather.{WeatherEndTurnResolver, WeatherSystem}
import scalamon.logics.weather.WeatherSystem.default
import Utilities.*

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
 */
enum TurnResult:
  case Ongoing
  case Victory(winnerName: String)
  case ForcedSwitch(requests: List[SwitchRequest])

trait TurnResolutionModule:
  def isKnockedOut(pokemon: PokemonState): Boolean
  def isDefeated(player: PlayerState): Boolean
  def needsForcedSwitch(player: PlayerState): Boolean
  def getTurnResults(state: BattleState): TurnResult
  def endTurn: List[StateTransformer]

object TurnResolutionImpl extends TurnResolutionModule:

  override def isKnockedOut(pokemon: PokemonState): Boolean = pokemon.currentHp <= 0

  override def isDefeated(player: PlayerState): Boolean = player.team.values.forall(isKnockedOut)

  override def needsForcedSwitch(player: PlayerState): Boolean = isKnockedOut(player.getActive) && !isDefeated(player)

  /**
   * Resolves the outcome of a turn, determining if the battle is ongoing,
   * if one side has won, or if a forced switch is required.
   */
  override def getTurnResults(state: BattleState): TurnResult =
    if isDefeated(state.self) then TurnResult.Victory(state.opponent.name)
    else if isDefeated(state.opponent) then TurnResult.Victory(state.self.name)
    else
      val requests = List(Side.Self, Side.Opponent).flatMap: side =>
        val player = playerOf(state, side)
        Option.when(needsForcedSwitch(player))(SwitchRequest(side, aliveBench(player)))
      if requests.isEmpty then TurnResult.Ongoing
      else TurnResult.ForcedSwitch(requests)

  private def applyStatusEffects: StateTransformer =
    forBothSides( s =>
      s.self.getActive.status.foldLeft(s)((st, status) => status.applyCondition(st))
    )

  private def applyEndOfTurnAbilities: StateTransformer = forBothSides(applyPassiveEffects(OnTurnEnd))

  private def applyWeatherEffects: StateTransformer = summon[WeatherEndTurnResolver].apply

  /**
   * Applies end-of-turn effects, including status effects, weather effects,
   * and abilities that trigger at the end of the turn.
   */
  override def endTurn: List[StateTransformer] = List(
    applyStatusEffects,
    applyWeatherEffects,
    applyEndOfTurnAbilities,
  )
