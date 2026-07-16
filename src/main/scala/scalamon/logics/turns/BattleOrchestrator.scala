package scalamon.logics.turns

import scalamon.domain.actions.{MoveAction, SwitchAction}
import scalamon.domain.moves.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.turns.TurnResolutionImpl.*
import scalamon.domain.moves.Accuracy.given
import scalamon.domain.pokemon.abilities.Target
import scalamon.domain.pokemon.abilities.Target.*
import scalamon.domain.pokemon.abilities.AbilityTrigger.*
import scalamon.logics.log.BattleLogger
import scalamon.logics.log.BattleLogger.emptyLogger
import scalamon.logics.turns.ActionOrder.*
import Utilities.*
import scalamon.logics.damage.DamagePolicy

/**
 * Coordinates the execution of a battle turn.
 */
final class BattleOrchestrator(using DamagePolicy):

  def runTurn(state: BattleState, choices: TurnChoices, speedOf: PlayerState => Speed): (TurnResult, BattleState) =
    val order = TurnFlow.actionOrdering(state, choices, speedOf)
    val allStateTransformers = startTurn concat orderedActions(order)(choices) concat endTurn
    val newState = allStateTransformers.foldLeft(state)((s, f) => f(s))
    (getTurnResults(newState), newState)

  private def startTurn: List[StateTransformer] = List(
    updateLogs(_ => emptyLogger),
    self(updateFlags(_.copy(magicGuardActive = false))),
    forBothSides(applyPassiveEffects(OnTurnStart)),
  )

  private def orderedActions(order: ActionOrder)(choices: TurnChoices): List[StateTransformer] =
    val turnOrder = List(
      executeAction(choices.player1Action),
      switchSelfOpponent,
      executeAction(choices.player2Action),
      switchSelfOpponent
    )
    order match
      case Player1First => turnOrder
      case Player2First => turnOrder.reverse

  /** Applies a forced switch for the given side, then its switch-in effects. */
  private def executeSwitch(side: Side, newActive: PokemonRef): StateTransformer =
    onSide(side)(
      applyPassiveEffects(OnSwitchOut(Self)) andThen
      SwitchAction(newActive.value) andThen
      applyPassiveEffects(OnSwitchIn(Self))
    )

  /** Applies a batch of forced switches in order (covers the "both KO" case). */
  def applyForcedSwitches(choices: List[(Side, PokemonRef)])(state: BattleState): BattleState =
    choices.foldLeft(state) { case (s, (side, ref)) => executeSwitch(side, ref)(s) }

  private def executeAction(battleAction: BattleAction)(state: BattleState): BattleState =
    battleAction match
      case UseMove(moveRef, _) =>
        if state.self.getActive.currentHp <= 0 then
          updateLogs(BattleLogger.logCannotMoveIsKo(state.self.getActive))(state)
        else
          executeMove(moveRef)(state)

      case SwitchPokemon(to) =>
        if state.self.flags.isSwitchBlocked then
          updateLogs(BattleLogger.logMessage("Switch is blocked"))(state)
        else
          executeSwitch(Side.Self, to)(state)

      case UseItem(name) =>
        state.self.items.find(_.name == name)
          .getOrElse(updateLogs(BattleLogger.logError(s"Item $name not found")))(state)

  private def executeMove(moveRef: MoveRef)(state: BattleState): BattleState =
    val activePokemon = state.self.getActive
    findMove(moveRef) match
      case Some(move) if !hasPpFor(activePokemon, move) =>
        updateLogs(BattleLogger.logNotEnoughPP(activePokemon, move))(state)
      case Some(move: DamageMove) if !canMove(activePokemon, move.moveType, state.weather) =>
        activePokemon.statusCondition match
          case Some(blockingStatus) => updateLogs(BattleLogger.logStatusPreventsMove(activePokemon, blockingStatus))(state)
          case None => updateLogs(BattleLogger.logCannotMove(activePokemon))(state)
      case Some(move: DamageMove) if isSelfHitting(activePokemon) =>
        updateLogs(BattleLogger.logSelfHit(activePokemon))(state)
      case Some(move: DamageMove) => executeDamageMove(move)(state)
      case Some(move: NonDamagingMove) => executeNonDamageMove(move)(state)
      case None => updateLogs(BattleLogger.logError(s"Move ${moveRef.value} not found"))(state)


  /**
   * Executes a damaging move; the manual flip/unflip dance is now expressed
   * through asOpponent, which makes the intent explicit.
   */
  private def executeDamageMove(move: DamageMove)(state: BattleState): BattleState =
    val withLastMove = self(updateFlags(_.copy(lastMove = Some(move))))(state)
    val afterMove = MoveAction(move)(withLastMove)
    val afterDamageDealt = applyPassiveEffects(OnDamageTaken(Opponent))(afterMove)
    val afterDamageTaken = asOpponent(applyPassiveEffects(OnDamageTaken(Self)))(afterDamageDealt)
    if isKnockedOut(afterDamageTaken.opponent.getActive) then
      val afterAttackerKO = applyPassiveEffects(OnKOTaken(Opponent))(afterDamageTaken)
      asOpponent(applyPassiveEffects(OnKOTaken(Self)))(afterAttackerKO)
    else
      afterDamageTaken

  private def executeNonDamageMove(move: Move)(state: BattleState): BattleState =
    val stateWithResetFlag = self(updateFlags(_.copy(lastMove = None)))(state)
    val afterMove = MoveAction(move)(stateWithResetFlag)
    asOpponent(applyPassiveEffects(OnDamageTaken(Self)))(afterMove)

