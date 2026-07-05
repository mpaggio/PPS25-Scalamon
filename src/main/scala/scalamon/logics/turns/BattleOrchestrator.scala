package scalamon.logics.turns

import scalamon.domain.moves.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.state.DamagePolicy
import scalamon.logics.turns.BattleAction
import scalamon.logics.turns.UseMove
import scalamon.logics.turns.SwitchPokemon
import scalamon.logics.turns.TurnResolutionImpl.*
import scalamon.domain.moves.MoveDatabase.findByName
import scalamon.logics.turns.TurnResult.BothForcedSwitch
import scalamon.logics.state.AlteredStatusModule.*
import scalamon.domain.moves.Accuracy.given
import scalamon.domain.pokemon.abilities.Target
import scalamon.domain.pokemon.abilities.Target.*
import scalamon.domain.pokemon.abilities.AbilityTrigger.*
import scalamon.domain.pokemon.abilities.{AbilityTrigger, MyAbilityBook}
import scalamon.logics.turns.ActionOrder.*

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
final class BattleOrchestrator(using DamagePolicy):
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
   * Weather is read directly from the battle state.
   *
   * @param state the current battle state
   * @param choices the actions selected for the turn
   * @param speedOf a function that returns the speed of a Pokémon reference
   * @return the updated battle state together with the resolved turn result
   */
  def runTurn(state: BattleState, choices: TurnChoices, speedOf: PlayerState => Speed): (BattleState, TurnResult) =
    val plan = TurnFlow.actionOrdering(state, choices, speedOf)
    val resetState = state.updateFlags(_.copy(selfMagicGuardActive = false))
    val afterTurnStart = applyTriggerForBoth(OnTurnStart)(resetState)
    val afterExecution = orderedActions(plan)(choices).foldLeft(afterTurnStart)((s, f) => f(s))
    val result = resolveTurn(afterExecution)
    val finalState = result match
      case TurnResult.Ongoing(s) => endTurn(s)
      case TurnResult.ForcedSwitch(s, _) => s
      case TurnResult.OpponentForcedSwitch(s, _) => s
      case TurnResult.BothForcedSwitch(s, _, _) => s
      case TurnResult.SelfWins(s) => s
      case TurnResult.SelfLoses(s) => s
    (finalState, result)

  private def orderedActions(plan: ActionOrder)(choices: TurnChoices): List[StateTransformer] = plan match
    case SelfFirst => List(
      executeAction(choices.first),
      switchSelfOpponent,
      executeAction(choices.second),
      switchSelfOpponent
    )
    case OpponentFirst => List(
      switchSelfOpponent,
      executeAction(choices.second),
      switchSelfOpponent,
      executeAction(choices.first)
    )

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
  private def executeAction(battleAction: BattleAction)(state: BattleState): BattleState =
    battleAction match
      case UseMove(moveRef, _) =>
        val attackerHp = state.self.getActive.currentHp
        if attackerHp <= 0 then
          println(s"  ${state.self.getActive.species.name} e' KO e non puo' attaccare!")
          state
        else
          val newState = executeMove(moveRef)(state)
          val defenderName = state.opponent.getActive.species.name
          val defenderHp = newState.opponent.getActive.currentHp
          println(s"  ${state.self.getActive.species.name} usa ${moveRef.value} | $defenderName HP: $defenderHp")
          newState

      case SwitchPokemon(to) =>
        val beforeSwitchOut = applyPassiveEffects(OnSwitchOut(Self))(state)
        val switched = self(switchActive(to.value))(beforeSwitchOut)
        applyPassiveEffects(OnSwitchIn(Self))(switched)
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
  private def executeMove(moveRef: MoveRef)(state: BattleState): BattleState =
    val activePokemon = state.self.getActive
    resolveMove(moveRef) match
      case Some(move: DamageMove) =>
        val currentMoveState = activePokemon.moveState(move.name)
        if currentMoveState.currentPp <= 0 then
          println(s" ${activePokemon.species.name} non ha abbastanza PP per user ${moveRef.value}!")
          state
        else if !canMove(activePokemon) then
          println(s" ${activePokemon.species.name} non puo' muoversi a causa del suo stato!")
          state
        else if isSelfHitting(activePokemon) then
          println(s" ${activePokemon.species.name} si colpisce da solo usando ${moveRef.value}!")
          MoveAction(move, Target.Self)(state)

        else
          val orientedWithMove = state.updateFlags(_.copy(lastOpponentMove = Some(move)))
          val afterMove = MoveAction(move)(orientedWithMove)
          val afterDamageDealt = applyPassiveEffects(OnDamageTaken(Opponent))(afterMove)
          val defenderIsKnockedOut = isKnockedOut(afterDamageDealt.opponent.getActive)
          if defenderIsKnockedOut then
            val afterAttackerKO = applyPassiveEffects(OnKOTaken(Opponent))(afterDamageDealt)
            val flipped = switchSelfOpponent(afterAttackerKO)
            val afterDefenderKO = applyPassiveEffects(OnKOTaken(Self))(flipped)
            switchSelfOpponent(afterDefenderKO)
          else
            afterDamageDealt
      case Some(move) =>
        val currentMoveState = activePokemon.moveState(move.name)
        if currentMoveState.currentPp <= 0 then
          println(s" ${activePokemon.species.name} non ha abbastanza PP per user ${moveRef.value}!")
          state
        else
          val afterMove = MoveAction(move)(state)
          val flipped = switchSelfOpponent(afterMove)
          println(s"DEBUG: ${flipped.self.getActive.species.name} status = ${flipped.self.getActive.statusCondition}")
          switchSelfOpponent(applyPassiveEffects(OnDamageTaken(Self))(flipped))
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
    val afterSelf = applyPassiveEffects(trigger)(state)
    val oriented = switchSelfOpponent(afterSelf)
    val afterOpponent = applyPassiveEffects(trigger)(oriented)
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
    applyPassiveEffects(OnSwitchIn(Self))(switched)

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
    val afterIn = applyPassiveEffects(OnSwitchIn(Self))(oriented)
    switchSelfOpponent(afterIn)

  private def applyPassiveEffects(trigger: Trigger)(bs: BattleState): BattleState =
    val newBs = MyAbilityBook.runTrigger(trigger, bs.self.getActive.species.abilitySlot)(bs)
    newBs.passiveEffects.foldLeft(newBs)((state, effect) => effect(trigger)(state))
