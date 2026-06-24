package scalamon.logics.turns

import scalamon.domain.moves.{Move, MoveDatabase}
import scalamon.domain.moves.MoveActionModuleImpl.*
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.DamagePolicy
import scalamon.logics.turns.BattleAction.*
import scalamon.logics.turns.TurnResolutionImpl.*
import scalamon.domain.moves.MoveActionModuleImpl.ProbabilityRoll
import scalamon.domain.moves.MoveDatabase.findByName
import scalamon.logics.turns.TurnResult.BothForcedSwitch

/**
 * Coordinates the execution and resolution of a battle turn.
 *
 * A battle orchestrator schedules the selected actions, executes them in order,
 * resolves the resulting turn outcome, and applies any end-of-turn updates when needed.
 *
 * @constructor
 *   creates a battle orchestrator using the provided turn flow
 * @param turnFlow
 *   the component used to schedule and order turn actions
 */
final class BattleOrchestrator(turnFlow: TurnFlow)(using DamagePolicy, ProbabilityRoll):
  /**
   * Executes a full turn starting from the current state and the chosen actions.
   *
   * The turn is scheduled, all actions are executed in order, the resulting
   * outcome is resolved, and end-of-turn effects are applied when the battle
   * remains ongoing.
   *
   * @param state
   * the current battle state
   * @param choices
   * the actions selected for the turn
   * @param speedOf
   * a function returning the speed of a Pokémon reference
   * @return
   * the updated battle state together with the resolved turn result
   */
  def runTurn(state: BattleState, choices: TurnChoices, speedOf: PokemonRef => Speed): (BattleState, TurnResult) =
    val plan = turnFlow.startTurn(choices, speedOf)
    val afterExecution = plan.orderedActions.foldLeft(state)(executeScheduled)
    val result = resolveTurn(afterExecution)
    val finalState = result match
      case TurnResult.Ongoing(s) => endTurn(s)
      case TurnResult.ForcedSwitch(s, _) => s
      case TurnResult.OpponentForcedSwitch(s, _) => s
      case TurnResult.BothForcedSwitch(s, _, _) => s
      case TurnResult.SelfWins(s) => s
      case TurnResult.SelfLoses(s) => s
    (finalState, result)

  private def executeScheduled(state: BattleState, scheduled: ScheduledAction): BattleState =
    scheduled.action match
      case UseMove(_, attacking, _, moveRef, _) =>
        val attackerHp = state.self.team.get(attacking.value)
          .orElse(state.opponent.team.get(attacking.value))
          .map(_.currentHp)
          .getOrElse(0)
        if attackerHp <= 0 then
          println(s"  ${attacking.value} è KO e non può attaccare!")
          state
        else
          val newState = executeMove(state, attacking, moveRef)
          val defenderRef = scheduled.action match
            case UseMove(_, _, defending, _, _) => defending.value
            case _ => "?"
          val defenderHp = newState.self.team.get(defenderRef)
            .orElse(newState.opponent.team.get(defenderRef))
            .map(_.currentHp)
            .getOrElse(0)
          println(s"  ${attacking.value} usa ${moveRef.value} → $defenderRef HP: $defenderHp")
          newState
      case SwitchPokemon(_, from, to, _) =>
        if state.self.team.contains(from.value) then
          state self (_ switchActive to.value)
        else
          state opponent (_ switchActive to.value)

  private def executeMove(state: BattleState, attacking: PokemonRef, moveRef: MoveRef): BattleState =
    val oriented =
      if state.self.team.contains(attacking.value) then state
      else state.switchUserEnemy
    val activePokemon = oriented.self.team(oriented.self.activeId)
    resolveMove(moveRef) match
      case Some(move) =>
        val currentMoveState = activePokemon.moveState(move.name)
        if currentMoveState.currentPp <= 0 then
          println(s" ${attacking.value} non ha abbastanza PP per user ${moveRef.value}!")
          state
        else
          val afterMove = MoveAction(move).execute.foldLeft(oriented)((s, f) => f(s))
          if state.self.team.contains(attacking.value) then afterMove
          else afterMove.switchUserEnemy
      case None => state

  private def resolveMove(moveRef: MoveRef): Option[Move] =
    MoveDatabase.allMoves.findByName(moveRef.value)

  def applyForcedSwitch(state: BattleState, newActive: PokemonRef): BattleState =
    val newSelf = TurnResolutionImpl.applyForcedSwitch(state.self, newActive)
    state.copy(self = newSelf)

  def applyOpponentForcedSwitch(state: BattleState, newActive: PokemonRef): BattleState =
    state opponent (_ switchActive newActive.value)