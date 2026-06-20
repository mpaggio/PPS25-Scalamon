package scalamon.logics.turns

import scalamon.domain.moves.{Move, MoveDatabase}
import scalamon.domain.moves.MoveActionModuleImpl.*
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.DamagePolicy
import scalamon.logics.turns.BattleAction.*
import scalamon.logics.turns.TurnResolutionImpl.*
import scalamon.domain.moves.MoveActionModuleImpl.ProbabilityRoll
import scalamon.domain.moves.MoveDatabase.findByName

final class BattleOrchestrator(turnFlow: TurnFlow)(using DamagePolicy, ProbabilityRoll):
  def runTurn(state: BattleState, choices: TurnChoises, speedOf: PokemonRef => Speed): (BattleState, TurnResult) =
    val plan = turnFlow.startTurn(choices, speedOf)
    val afterExecution = plan.orderedActions.foldLeft(state)(executeScheduled)
    val result = resolveTurn(afterExecution)
    val finalState = result match
      case TurnResult.Ongoing(s) => endTurn(s)
      case TurnResult.ForcedSwitch(s, _) => s
      case TurnResult.SelfWins(s) => s
      case TurnResult.SelfLoses(s) => s
    (finalState, result)

  private def executeScheduled(state: BattleState, scheduled: ScheduledAction): BattleState =
    scheduled.action match
      case UseMove(_, attacking, _, moveRef, _) =>  executeMove(state, attacking, moveRef)
      case SwitchPokemon(_, from, to, _) =>
        if state.self.team.contains(from.value) then
          state self (_ switchActive to.value)
        else
          state opponent (_ switchActive to.value)

  private def executeMove(state: BattleState, attacking: PokemonRef, moveRef: MoveRef): BattleState =
    val oriented =
      if state.self.team.contains(attacking.value) then state
      else state.switchUserEnemy
    resolveMove(moveRef) match
      case Some(move) =>
        val afterMove =
          MoveAction(move).execute.foldLeft(oriented)((s, f) => f(s))
        if state.self.team.contains(attacking.value) then afterMove
        else afterMove.switchUserEnemy
      case None => state


  private def resolveMove(moveRef: MoveRef): Option[Move] =
    MoveDatabase.allMoves.findByName(moveRef.value)
