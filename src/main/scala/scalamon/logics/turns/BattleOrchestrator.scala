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
 */
final class BattleOrchestrator(using DamagePolicy):

  def runTurn(state: BattleState, choices: TurnChoices, speedOf: PlayerState => Speed): (TurnResult, BattleState) =
    val plan = TurnFlow.actionOrdering(state, choices, speedOf)
    val resetState = updateLogs(_ => emptyLogger)(self(_.updateFlags(_.copy(magicGuardActive = false)))(state))
    val afterTurnStart = forBothSides(applyPassiveEffects(OnTurnStart))(resetState)
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

  /** Applies a forced switch for the given side, then its switch-in effects. */
  def applyForcedSwitch(side: Side, newActive: PokemonRef)(state: BattleState): BattleState =
    onSide(side)( oriented =>
      val switched = oriented.copy(self = TurnResolutionImpl.applyForcedSwitch(oriented.self, newActive))
      applyPassiveEffects(OnSwitchIn(Self))(switched)
    )(state)

  /** Applies a batch of forced switches in order (covers the "both KO" case). */
  def applyForcedSwitches(choices: List[(Side, PokemonRef)])(state: BattleState): BattleState =
    choices.foldLeft(state) { case (s, (side, ref)) => applyForcedSwitch(side, ref)(s) }


  private def applyPassiveEffects(trigger: Trigger)(bs: BattleState): BattleState =
    val newBs = MyAbilityBook.runTrigger(trigger, bs.self.getActive.species.abilitySlot)(bs)
    newBs.passiveEffects.foldLeft(newBs)((state, effect) => effect(trigger)(state))

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
          val beforeSwitchOut = applyPassiveEffects(OnSwitchOut(Self))(state)
          val switched = SwitchAction(to.value)(beforeSwitchOut)
          applyPassiveEffects(OnSwitchIn(Self))(switched)

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

  private def findMove(moveRef: MoveRef): Option[Move] =
    MoveDatabase.allMoves.findByName(moveRef.value)

  private def hasPpFor(pokemon: PokemonState, move: Move): Boolean =
    pokemon.moveState(move.name).currentPp > 0

  private def canMove(pokemon: PokemonState, moveType: Type, currentWeather: Weather): Boolean =
    pokemon.statusCondition.forall(_.canMove(moveType, currentWeather))

  private def isSelfHitting(pokemon: PokemonState): Boolean =
    pokemon.statusCondition.exists(_.isSelfHitting)

  /**
   * Executes a damaging move; the manual flip/unflip dance is now expressed
   * through asOpponent, which makes the intent explicit.
   */
  private def executeDamageMove(move: DamageMove)(state: BattleState): BattleState =
    val withLastMove = self(_.updateFlags(_.copy(lastMove = Some(move))))(state)
    val afterMove = MoveAction(move)(withLastMove)
    val afterDamageDealt = applyPassiveEffects(OnDamageTaken(Opponent))(afterMove)
    val afterDamageTaken = asOpponent(applyPassiveEffects(OnDamageTaken(Self)))(afterDamageDealt)
    if isKnockedOut(afterDamageTaken.opponent.getActive) then
      val afterAttackerKO = applyPassiveEffects(OnKOTaken(Opponent))(afterDamageTaken)
      asOpponent(applyPassiveEffects(OnKOTaken(Self)))(afterAttackerKO)
    else
      afterDamageTaken

  private def executeNonDamageMove(move: Move)(state: BattleState): BattleState =
    val stateWithResetFlag = self(_.updateFlags(_.copy(lastMove = None)))(state)
    val afterMove = MoveAction(move)(stateWithResetFlag)
    asOpponent(applyPassiveEffects(OnDamageTaken(Self)))(afterMove)

  /** Runs f from the perspective of the given side, restoring orientation. */
  private def onSide(side: Side)(f: StateTransformer): StateTransformer =
    side match
      case Side.Self => f
      case Side.Opponent => s => switchSelfOpponent(f(switchSelfOpponent(s)))

  /** Runs f from the opponent's perspective, restoring orientation. */
  private def asOpponent(f: StateTransformer): StateTransformer =
    onSide(Side.Opponent)(f)

  /** Runs f once per side, each time from that side's perspective. */
  private def forBothSides(f: StateTransformer): StateTransformer =
    state => List(Side.Self, Side.Opponent).foldLeft(state)((s, side) => onSide(side)(f)(s))
