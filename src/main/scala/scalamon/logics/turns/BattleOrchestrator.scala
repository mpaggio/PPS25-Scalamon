package scalamon.logics.turns

import scalamon.domain.actions.SwitchAction
import scalamon.domain.moves.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.state.DamagePolicy
import scalamon.logics.turns.TurnResolutionImpl.*
import scalamon.domain.moves.MoveDatabase.findByName
import scalamon.logics.state.AlteredStatusModule.*
import scalamon.domain.moves.Accuracy.given
import scalamon.domain.pokemon.abilities.Target
import scalamon.domain.pokemon.abilities.Target.*
import scalamon.domain.pokemon.abilities.AbilityTrigger.*
import scalamon.domain.pokemon.abilities.{AbilityTrigger, MyAbilityBook}
import scalamon.domain.types.Type
import scalamon.logics.log.BattleLogger
import scalamon.logics.log.BattleLogger.emptyLogger
import scalamon.logics.turns.ActionOrder.*
import scalamon.logics.state.BattleStateImpl.{opponent, self}

/**
 * Coordinates the execution of a battle turn.
 *
 * This orchestrator is responsible for ordering the selected actions,
 * applying turn-based triggers, executing moves and switches, and
 * resolving the final turn outcome.
 *
 * @param DamagePolicy
 *   the damage policy used to resolve damaging moves
 */
final class BattleOrchestrator(using DamagePolicy):

  /**
   * Executes a full battle turn starting from the provided state.
   *
   * The turn flow includes action ordering, reset of turn-scoped flags and logs,
   * application of start-of-turn ability triggers, execution of both players'
   * chosen actions, and final turn resolution.
   *
   * @param state
   *   the battle state at the beginning of the turn
   * @param choices
   *   the actions selected by the two players for the current turn
   * @param speedOf
   *   a function used to extract the speed of a player state for turn ordering
   * @return
   *   a pair containing the resolved turn result and the updated battle state
   */
  def runTurn(state: BattleState, choices: TurnChoices, speedOf: PlayerState => Speed): (TurnResult, BattleState) =
    val plan = TurnFlow.actionOrdering(state, choices, speedOf)
    val resetState = updateLogs(_ => emptyLogger)(self(_.updateFlags(_.copy(magicGuardActive = false)))(state))
    val afterTurnStart = applyTriggerForBoth(OnTurnStart)(resetState)
    val afterExecution = orderedActions(plan)(choices).foldLeft(afterTurnStart)((s, f) => f(s))
    resolveTurn(afterExecution)

  private def orderedActions(plan: ActionOrder)(choices: TurnChoices): List[StateTransformer] =
    val turnOrder = List(
      executeAction(choices.player1Action),
      switchSelfOpponent,
      executeAction(choices.player2Action),
      switchSelfOpponent
    )

    plan match
      case Player1First => turnOrder
      case Player2First => turnOrder.reverse

  /**
   * Executes a forced switch for the current player.
   *
   * The new active Pokémon is applied immediately and then the corresponding
   * switch-in passive effects are triggered from the current player's perspective.
   *
   * @param state
   *   the current battle state
   * @param newActive
   *   the identifier of the Pokémon that must become active
   * @return
   *   the updated battle state after the forced switch and switch-in effects
   */
  def applyForcedSwitch(state: BattleState, newActive: PokemonRef): BattleState =
    val newSelf = TurnResolutionImpl.applyForcedSwitch(state.self, newActive)
    val switched = state.copy(self = newSelf)
    applyPassiveEffects(OnSwitchIn(Self))(switched)

  /**
   * Executes a forced switch for the opponent.
   *
   * The battle state is temporarily reoriented so that switch-in passive effects
   * can be resolved from the switched player's perspective before restoring the
   * original orientation.
   *
   * @param state
   *   the current battle state
   * @param newActive
   *   the identifier of the opponent's Pokémon that must become active
   * @return
   *   the updated battle state after the opponent forced switch and switch-in effects
   */
  def applyOpponentForcedSwitch(state: BattleState, newActive: PokemonRef): BattleState =
    val switched = opponent(switchActive(newActive.value))(state)
    val oriented = switchSelfOpponent(switched)
    val afterIn = applyPassiveEffects(OnSwitchIn(Self))(oriented)
    switchSelfOpponent(afterIn)

  /**
   * Applies passive effects associated with the given trigger to the current active Pokémon.
   *
   * This includes both the effects produced directly by the active Pokémon ability
   * and any passive effects already registered in the battle state.
   *
   * @param trigger
   *   the trigger that activates the passive effects
   * @param bs
   *   the battle state on which the effects are evaluated
   * @return
   *   the updated battle state after all matching passive effects have been applied
   */
  private def applyPassiveEffects(trigger: Trigger)(bs: BattleState): BattleState =
    val newBs = MyAbilityBook.runTrigger(trigger, bs.self.getActive.species.abilitySlot)(bs)
    newBs.passiveEffects.foldLeft(newBs)((state, effect) => effect(trigger)(state))

  private def executeAction(battleAction: BattleAction)(state: BattleState): BattleState =
    battleAction match
      case UseMove(moveRef, _) =>
        val attackerHp = state.self.getActive.currentHp
        if attackerHp <= 0 then
          updateLogs(BattleLogger.logCannotMoveIsKo(state.self.getActive))(state)
        else
          executeMove(moveRef)(state)

      case SwitchPokemon(to) =>
        if state.self.flags.isSwitchBlocked then
          updateLogs(BattleLogger.logMessage("Switch is blocked"))(state)
        else
          val beforeSwitchOut = applyPassiveEffects(OnSwitchOut(Self))(state)
          val switched = SwitchAction(to.value)(beforeSwitchOut)
          applyPassiveEffects(OnSwitchIn(Self))(switched)

      case UseItem(name) => state.self.items.find(_.name == name)
        .getOrElse(updateLogs(BattleLogger.logError(s"Item $name not found")))(state)

  /**
   * Resolves the execution of a selected move for the current active Pokémon.
   *
   * This method validates move availability, checks whether the Pokémon can act,
   * and dispatches execution to the appropriate damaging or non-damaging move handler.
   *
   * @param moveRef
   *   the identifier of the move to execute
   * @param state
   *   the current battle state
   * @return
   *   the updated battle state after move resolution or failure logging
   */
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

  private def findMove(moveRef: MoveRef): Option[Move] =
    MoveDatabase.allMoves.findByName(moveRef.value)

  private def hasPpFor(pokemon: PokemonState, move: Move): Boolean =
    pokemon.moveState(move.name).currentPp > 0

  private def canMove(pokemon: PokemonState, moveType: Type, currentWeather: Weather): Boolean =
    pokemon.statusCondition.forall(_.canMove(moveType, currentWeather))

  private def isSelfHitting(pokemon: PokemonState): Boolean =
    pokemon.statusCondition.exists(_.isSelfHitting)

  /**
   * Executes a damaging move and resolves its follow-up passive effects.
   *
   * After move execution, this method applies damage-related triggers for both sides
   * and, if the defender is knocked out, also resolves KO-related passive effects.
   *
   * @param move
   *   the damaging move to execute
   * @param state
   *   the current battle state
   * @return
   *   the updated battle state after damage and related triggers have been resolved
   */
  private def executeDamageMove(move: DamageMove)(state: BattleState): BattleState =
    val withLastMove = self(_.updateFlags(_.copy(lastMove = Some(move))))(state)
    val afterMove = MoveAction(move)(withLastMove)
    val afterDamageDealt = applyPassiveEffects(OnDamageTaken(Opponent))(afterMove)
    val flippedDamageDealt = switchSelfOpponent(afterDamageDealt)
    val afterDamageTaken = switchSelfOpponent(applyPassiveEffects(OnDamageTaken(Self))(flippedDamageDealt))
    if isKnockedOut(afterDamageTaken.opponent.getActive) then
      val afterAttackerKO = applyPassiveEffects(OnKOTaken(Opponent))(afterDamageTaken)
      val flipped = switchSelfOpponent(afterAttackerKO)
      val afterDefenderKO = applyPassiveEffects(OnKOTaken(Self))(flipped)
      switchSelfOpponent(afterDefenderKO)
    else
      afterDamageTaken

  /**
   * Executes a non-damaging move and resolves its related passive effects.
   *
   * Non-damaging moves clear the last damaging move flag before execution and
   * still trigger the relevant post-action passive effects on the affected side.
   *
   * @param move
   *   the non-damaging move to execute
   * @param state
   *   the current battle state
   * @return
   *   the updated battle state after move execution and related triggers
   */
  private def executeNonDamageMove(move: Move)(state: BattleState): BattleState =
    val stateWithResetFlag = self(_.updateFlags(_.copy(lastMove = None)))(state)
    val afterMove = MoveAction(move)(stateWithResetFlag)
    val flipped = switchSelfOpponent(afterMove)
    switchSelfOpponent(applyPassiveEffects(OnDamageTaken(Self))(flipped))

  /**
   * Applies the same ability trigger to both sides of the battle.
   *
   * The battle state is reoriented between applications so that each side
   * resolves the trigger from its own perspective.
   *
   * @param trigger
   *   the ability trigger to apply
   * @param state
   *   the battle state on which the trigger is resolved
   * @return
   *   the updated battle state after both sides have processed the trigger
   */
  private def applyTriggerForBoth(trigger: AbilityTrigger)(state: BattleState): BattleState =
    val afterSelf = applyPassiveEffects(trigger)(state)
    val oriented = switchSelfOpponent(afterSelf)
    val afterOpponent = applyPassiveEffects(trigger)(oriented)
    switchSelfOpponent(afterOpponent)