package scalamon.logics.turns

import scalamon.domain.moves.{DamageMove, Move, MoveDatabase}
import scalamon.domain.moves.MoveActionModuleImpl.*
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.DamagePolicy
import scalamon.logics.turns.BattleAction.*
import scalamon.logics.turns.TurnResolutionImpl.*
import scalamon.domain.moves.MoveDatabase.findByName
import scalamon.logics.turns.TurnResult.BothForcedSwitch
import scalamon.logics.state.AlteredStatusModule.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.domain.moves.Accuracy.given
import scalamon.domain.moves.EffectTarget.Self
import scalamon.domain.pokemon.abilities.AbilityTrigger.{OnDamageDealt, OnDamageTaken, OnKODealt, OnSwitchIn, OnSwitchOut, OnTurnStart}
import scalamon.domain.pokemon.abilities.{AbilityTrigger, MyAbilityBook}
import scalamon.logics.battle.WeatherState
import scalamon.logics.state.PlayerStateModuleImpl.switchActive

/**
 * Coordinates the execution of a battle turn.
 *
 * The orchestrator schedules the chosen actions, applies start-of-turn ability
 * triggers, executes the scheduled actions in order, resolves the turn outcome,
 * and applies end-of-turn effects when the battle remains ongoing.
 *
 * @constructor creates a battle orchestrator using the provided turn flow
 * @param turnFlow the component used to schedule and order turn actions
 */
final class BattleOrchestrator(turnFlow: TurnFlow)(using DamagePolicy):
  /**
   * Executes one complete turn from the current battle state.
   *
   * The method:
   *   1. builds the ordered action plan from the selected choices,
   *   2. resets transient flags,
   *   3. applies start-of-turn ability triggers,
   *   4. executes all scheduled actions in order,
   *   5. resolves the resulting battle outcome,
   *   6. applies end-of-turn effects only if the turn remains ongoing.
   *
   * @param state the current battle state
   * @param choices the actions selected for the turn
   * @param weatherState the current weather context used by end-of-turn logic
   * @param speedOf a function that returns the speed of a Pokémon reference
   * @return the updated battle state together with the resolved turn result
   */
  def runTurn(state: BattleState, choices: TurnChoices, weatherState: WeatherState, speedOf: PokemonRef => Speed): (BattleState, TurnResult) =
    val plan = turnFlow.startTurn(choices, speedOf)
    val resetState = state.updateFlags(_.copy(selfMagicGuardActive = false))
    val afterTurnStart = applyTriggerForBoth(OnTurnStart)(resetState)
    val afterExecution = plan.orderedActions.foldLeft(afterTurnStart)(executeScheduled)
    val result = resolveTurn(afterExecution)
    val finalState = result match
      case TurnResult.Ongoing(s) => endTurn(s, weatherState)
      case TurnResult.ForcedSwitch(s, _) => s
      case TurnResult.OpponentForcedSwitch(s, _) => s
      case TurnResult.BothForcedSwitch(s, _, _) => s
      case TurnResult.SelfWins(s) => s
      case TurnResult.SelfLoses(s) => s
    (finalState, result)

  /**
   * Executes a single scheduled action against the given battle state.
   *
   * Move actions are delegated to `executeMove`, while switch actions apply the
   * appropriate switch-related ability triggers before and after the swap.
   *
   * @param state     the current battle state
   * @param scheduled the action to execute
   * @return the updated battle state after the scheduled action
   */
  private def executeScheduled(state: BattleState, scheduled: ScheduledAction): BattleState =
    scheduled.action match
      case UseMove(_, attacking, _, moveRef, _) =>
        val attackerHp = state.self.team.get(attacking.value)
          .orElse(state.opponent.team.get(attacking.value))
          .map(_.currentHp)
          .getOrElse(0)
        if attackerHp <= 0 then
          println(s"  ${attacking.value} e' KO e non puo' attaccare!")
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
          println(s"  ${attacking.value} usa ${moveRef.value} | $defenderRef HP: $defenderHp")
          newState
      case SwitchPokemon(_, from, to, _) =>
        val oriented = if state.self.team.contains(from.value) then state
        else switchSelfOpponent(state)
        val currentSlot = oriented.self.getActive.species.abilitySlot
        val afterSwitchOut = MyAbilityBook.runTrigger(OnSwitchOut, currentSlot)(oriented)
        val switched = self(switchActive(to.value))(afterSwitchOut)
        val newSlot = switched.self.getActive.species.abilitySlot
        val afterSwitchIn = MyAbilityBook.runTrigger(OnSwitchIn, newSlot)(switched)
        if state.self.team.contains(from.value) then afterSwitchIn
        else switchSelfOpponent(afterSwitchIn)

  /**
   * Executes one move from the point of view of the attacking Pokémon.
   *
   * The state is oriented so that the attacker is treated as `self`, then the
   * move is resolved, PP and status checks are applied, and relevant ability
   * triggers are fired after damage is dealt and after a KO is inflicted.
   *
   * Self-targeting moves are handled separately and the original orientation is
   * restored before returning.
   *
   * @param state     the current battle state
   * @param attacking the Pokémon reference performing the move
   * @param moveRef   the move reference selected by the attacker
   * @return the updated battle state after the move execution
   */
  private def executeMove(state: BattleState, attacking: PokemonRef, moveRef: MoveRef): BattleState =
    val oriented =
      if state.self.team.contains(attacking.value) then state
      else switchSelfOpponent(state)
    val activePokemon = oriented.self.team(oriented.self.activeId)
    resolveMove(moveRef) match
      case Some(move: DamageMove) =>
        val currentMoveState = activePokemon.moveState(move.name)
        if currentMoveState.currentPp <= 0 then
          println(s" ${attacking.value} non ha abbastanza PP per user ${moveRef.value}!")
          state
        else if !canMove(activePokemon) then
          println(s" ${attacking.value} non puo' muoversi a causa del suo stato!")
          restoreOrientation(state, attacking, oriented)
        else if isSelfHitting(activePokemon) then
          println(s" ${attacking.value} si colpisce da solo usando ${moveRef.value}!")
          val selfTargeted = MoveAction(move).execute(Self).foldLeft(oriented)((s, f) => f(s))
          restoreOrientation(state, attacking, selfTargeted)
        else
          val orientedWithMove = oriented.updateFlags(_.copy(lastOpponentMove = Some(move)))
          val afterMove = MoveAction(move).execute().foldLeft(orientedWithMove)((s, f) => f(s))
          val attackerSlot = afterMove.self.getActive.species.abilitySlot
          val afterDamageDealt = MyAbilityBook.runTrigger(OnDamageDealt, attackerSlot)(afterMove)
          val defenderIsKnockedOut = isKnockedOut(afterDamageDealt.opponent.getActive)
          if defenderIsKnockedOut then {
            val afterAttackerKO = MyAbilityBook.runTrigger(OnKODealt, attackerSlot)(afterDamageDealt)
            val flipped = switchSelfOpponent(afterAttackerKO)
            val defenderSlot = flipped.self.getActive.species.abilitySlot
            val afterDefenderKO = MyAbilityBook.runTrigger(OnKODealt, defenderSlot)(flipped)
            switchSelfOpponent(afterDefenderKO)
          } else
          restoreOrientation(state, attacking, afterDamageDealt)
      case Some(move) =>
        val currentMoveState = activePokemon.moveState(move.name)
        if currentMoveState.currentPp <= 0 then
          println(s" ${attacking.value} non ha abbastanza PP per user ${moveRef.value}!")
          state
        else
          val afterMove = MoveAction(move).execute().foldLeft(oriented)((s, f) => f(s))
          val flipped = switchSelfOpponent(afterMove)
          println(s"DEBUG: ${flipped.self.getActive.species.name} status = ${flipped.self.getActive.statusCondition}")
          val defenderSlot = flipped.self.getActive.species.abilitySlot
          val afterTrigger = switchSelfOpponent(MyAbilityBook.runTrigger(OnDamageTaken, defenderSlot)(flipped))
          restoreOrientation(state, attacking, afterTrigger)
      case None => state

  /**
   * Resolves a move reference into a concrete move definition.
   *
   * @param moveRef the move reference to look up
   * @return the move if it exists in the database, otherwise `None`
   */
  private def resolveMove(moveRef: MoveRef): Option[Move] =
    MoveDatabase.allMoves.findByName(moveRef.value)

  /**
   * Returns whether the given Pokémon can act in the current state.
   *
   * @param pokemon the Pokémon to inspect
   * @return `true` if the Pokémon is allowed to move, otherwise `false`
   */
  private def canMove(pokemon: PokemonState): Boolean =
    pokemon.statusCondition.forall(_.canMove)

  /**
   * Returns whether the given Pokémon is currently self-hitting.
   *
   * @param pokemon the Pokémon to inspect
   * @return `true` if the Pokémon is in a self-hitting status, otherwise `false`
   */
  private def isSelfHitting(pokemon: PokemonState): Boolean =
    pokemon.statusCondition.exists(_.isSelfHitting)

  /**
   * Restores the original battle orientation after an oriented operation.
   *
   * If the attacker belonged to the original `self` side, the oriented state is
   * returned as-is; otherwise the state is re-flipped back to the original view.
   *
   * @param original  the original battle state before orientation
   * @param attacking the Pokémon that was acting in the oriented view
   * @param oriented  the battle state produced while oriented around the attacker
   * @return the state restored to the original orientation
   */
  private def restoreOrientation(original: BattleState, attacking: PokemonRef, oriented: BattleState): BattleState =
    if original.self.team.contains(attacking.value) then oriented
    else switchSelfOpponent(oriented)

  /**
   * Applies the given ability trigger to both active Pokémon in order.
   *
   * The trigger is first applied to the current `self` active Pokémon, then the
   * state is flipped and the same trigger is applied to the other active Pokémon.
   * The original orientation is restored before returning.
   *
   * @param trigger the ability trigger to apply
   * @param state   the battle state on which the trigger should run
   * @return the updated battle state after applying the trigger to both sides
   */
  private def applyTriggerForBoth(trigger: AbilityTrigger)(state: BattleState): BattleState =
    val afterSelf = MyAbilityBook.runTrigger(trigger, state.self.getActive.species.abilitySlot)(state)
    val oriented = switchSelfOpponent(afterSelf)
    val afterOpponent = MyAbilityBook .runTrigger(trigger, oriented.self.getActive.species.abilitySlot)(oriented)
    switchSelfOpponent(afterOpponent)

  /**
   * Applies a forced switch to the self side of the provided battle state.
   *
   * @param state     the battle state to update
   * @param newActive the Pokémon that should become active
   * @return the updated battle state with the requested active Pokémon
   */
  def applyForcedSwitch(state: BattleState, newActive: PokemonRef): BattleState =
    val newSelf = TurnResolutionImpl.applyForcedSwitch(state.self, newActive)
    val switched = state.copy(self = newSelf)
    val slotIn = switched.self.getActive.species.abilitySlot
    MyAbilityBook.runTrigger(OnSwitchIn, slotIn)(switched)

  /**
   * Applies a forced switch to the opponent side of the provided battle state.
   *
   * @param state     the battle state to update
   * @param newActive the Pokémon that should become active
   * @return the updated battle state with the requested opponent active Pokémon
   */
  def applyOpponentForcedSwitch(state: BattleState, newActive: PokemonRef): BattleState =
    val switched = opponent(switchActive(newActive.value))(state)
    val oriented = switchSelfOpponent(switched)
    val slotIn = oriented.self.getActive.species.abilitySlot
    val afterIn = MyAbilityBook.runTrigger(OnSwitchIn, slotIn)(oriented)
    switchSelfOpponent(afterIn)
