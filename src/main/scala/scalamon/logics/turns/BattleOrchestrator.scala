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
import scalamon.logics.log.BattleLogger
import scalamon.logics.log.BattleLogger.emptyLogger
import scalamon.logics.turns.ActionOrder.*

final class BattleOrchestrator(using DamagePolicy):
  
  def runTurn(state: BattleState, choices: TurnChoices, speedOf: PlayerState => Speed): (TurnResult, BattleState) =
    val plan = TurnFlow.actionOrdering(state, choices, speedOf)
    val resetState = updateLogs(_ => emptyLogger)(state.updateFlags(_.copy(selfMagicGuardActive = false)))
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

  private def executeAction(battleAction: BattleAction)(state: BattleState): BattleState =
    battleAction match
      case UseMove(moveRef, _) =>
        val attackerHp = state.self.getActive.currentHp
        if attackerHp <= 0 then
          updateLogs(BattleLogger.logCannotMoveIsKo(state.self.getActive))(state)
        else
          executeMove(moveRef)(state)

      case SwitchPokemon(to) =>
        if state.flags.opponentSwitchBlocked then
          updateLogs(BattleLogger.logMessage("Switch is blocked"))(state)
        else
          val beforeSwitchOut = applyPassiveEffects(OnSwitchOut(Self))(state)
          val switched = SwitchAction(to.value)(beforeSwitchOut)
          applyPassiveEffects(OnSwitchIn(Self))(switched)

      case UseItem(name) => state.self.items.find(_.name == name)
        .getOrElse(updateLogs(BattleLogger.logError(s"Item $name not found")))(state)


  private def executeMove(moveRef: MoveRef)(state: BattleState): BattleState =
    val activePokemon = state.self.getActive
    findMove(moveRef) match
      case Some(move) if !hasPpFor(activePokemon, move) =>
        updateLogs(BattleLogger.logNotEnoughPP(activePokemon, move))(state)
      case Some(move: DamageMove) if !canMove(activePokemon) =>
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

  private def canMove(pokemon: PokemonState): Boolean =
    pokemon.statusCondition.forall(_.canMove)

  private def isSelfHitting(pokemon: PokemonState): Boolean =
    pokemon.statusCondition.exists(_.isSelfHitting)

  private def executeDamageMove(move: DamageMove)(state: BattleState): BattleState =
    val withLastMove = state.updateFlags(_.copy(lastOpponentMove = Some(move)))
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

  private def executeNonDamageMove(move: Move)(state: BattleState): BattleState =
    val stateWithResetFlag = state.updateFlags(_.copy(lastOpponentMove = None))
    val afterMove = MoveAction(move)(stateWithResetFlag)
    val flipped = switchSelfOpponent(afterMove)
    switchSelfOpponent(applyPassiveEffects(OnDamageTaken(Self))(flipped))

  private def applyTriggerForBoth(trigger: AbilityTrigger)(state: BattleState): BattleState =
    val afterSelf = applyPassiveEffects(trigger)(state)
    val oriented = switchSelfOpponent(afterSelf)
    val afterOpponent = applyPassiveEffects(trigger)(oriented)
    switchSelfOpponent(afterOpponent)

  def applyForcedSwitch(state: BattleState, newActive: PokemonRef): BattleState =
    val newSelf = TurnResolutionImpl.applyForcedSwitch(state.self, newActive)
    val switched = state.copy(self = newSelf)
    applyPassiveEffects(OnSwitchIn(Self))(switched)

  def applyOpponentForcedSwitch(state: BattleState, newActive: PokemonRef): BattleState =
    val switched = opponent(switchActive(newActive.value))(state)
    val oriented = switchSelfOpponent(switched)
    val afterIn = applyPassiveEffects(OnSwitchIn(Self))(oriented)
    switchSelfOpponent(afterIn)

  private def applyPassiveEffects(trigger: Trigger)(bs: BattleState): BattleState =
    val newBs = MyAbilityBook.runTrigger(trigger, bs.self.getActive.species.abilitySlot)(bs)
    newBs.passiveEffects.foldLeft(newBs)((state, effect) => effect(trigger)(state))
