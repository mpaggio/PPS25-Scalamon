package scalamon.logics.turns

import scalamon.domain.actions.Items
import scalamon.domain.moves.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.state.DamagePolicy
import scalamon.logics.turns.TurnResolutionImpl.*
import scalamon.domain.moves.MoveDatabase.findByName
import scalamon.logics.turns.TurnResult.BothForcedSwitch
import scalamon.logics.state.AlteredStatusModule.*
import scalamon.domain.moves.Accuracy.given
import scalamon.domain.pokemon.abilities.Target
import scalamon.domain.pokemon.abilities.Target.*
import scalamon.domain.pokemon.abilities.AbilityTrigger.*
import scalamon.domain.pokemon.abilities.{AbilityTrigger, MyAbilityBook}
import scalamon.logics.log.BattleLogger
import scalamon.logics.log.BattleLogger.{BattleLogger, emptyLogger}
import scalamon.logics.turns.ActionOrder.*

final class BattleOrchestrator(using DamagePolicy):
  
  def runTurn(state: BattleState, choices: TurnChoices, speedOf: PlayerState => Speed): TurnResult =
    val logger = BattleLogger.emptyLogger
    val plan = TurnFlow.actionOrdering(state, choices, speedOf)
    val resetState = state.updateFlags(_.copy(selfMagicGuardActive = false))
    val afterTurnStart = applyTriggerForBoth(OnTurnStart)(resetState)
    val afterExecution = orderedActions(plan)(choices).foldLeft((afterTurnStart, logger))((s, f) => f(s))
    resolveTurn(afterExecution._1)

  private def orderedActions(plan: ActionOrder)(choices: TurnChoices): List[LoggedState => LoggedState] =
    val turnOrder = List(
      executeAction(choices.player1Action),
      switchSelfOpponentPlusLog,
      executeAction(choices.player2Action),
      switchSelfOpponentPlusLog
    )
    
    plan match
      case Player1First => turnOrder
      case Player2First => turnOrder.reverse

  private def executeAction(battleAction: BattleAction)(loggedState: LoggedState): LoggedState =
    val state = loggedState._1
    val logger = loggedState._2
    battleAction match
      case UseMove(moveRef, _) =>
        val attackerHp = state.self.getActive.currentHp
        if attackerHp <= 0 then
          (state, logger.logIsKo(state.self.getActive))
        else
          executeMove(moveRef)(state, logger)

      case SwitchPokemon(to) =>
        val previousActive = state.self.getActive
        val beforeSwitchOut = applyPassiveEffects(OnSwitchOut(Self))(state)
        val switched = self(switchActive(to.value))(beforeSwitchOut)
        val finaleState = applyPassiveEffects(OnSwitchIn(Self))(switched)
        (finaleState, logger.logSwitchPokemon(previousActive, finaleState.self.getActive))

      case UseItem(name) => 
        val itemOption = state.self.items.find(_.name == name)
        (itemOption.getOrElse(Items.nullItem)(state), logger.logUseItem(state.self.getActive, itemOption))


  private def executeMove(moveRef: MoveRef)(loggedState: LoggedState): LoggedState =
    val state = loggedState.state
    val logger = loggedState.log
    val activePokemon = state.self.getActive
    findMove(moveRef) match
      case Some(move) if !hasPpFor(activePokemon, move) => (state, logger.logNotEnoughPP(activePokemon, move))
      case Some(move: DamageMove) if !canMove(activePokemon) => (state, logger.logCannotMove(activePokemon))
      case Some(move: DamageMove) if isSelfHitting(activePokemon) =>
        (MoveAction(move, Target.Self)(state), logger.logSelfHit(activePokemon, move))
      case Some(move: DamageMove) => executeDamageMove(move)(loggedState)
      case Some(move: NonDamagingMove) => executeNonDamageMove(move)(loggedState)
      case None => (state, logger.logMoveNotFound(activePokemon, moveRef))
      
  private def switchSelfOpponentPlusLog(state: LoggedState): LoggedState =
    (switchSelfOpponent(state._1), state._2)

  private def findMove(moveRef: MoveRef): Option[Move] =
    MoveDatabase.allMoves.findByName(moveRef.value)

  private def hasPpFor(pokemon: PokemonState, move: Move): Boolean =
    pokemon.moveState(move.name).currentPp > 0

  private def canMove(pokemon: PokemonState): Boolean =
    pokemon.statusCondition.forall(_.canMove)

  private def isSelfHitting(pokemon: PokemonState): Boolean =
    pokemon.statusCondition.exists(_.isSelfHitting)

  private def executeDamageMove(move: DamageMove)(loggedState: LoggedState): LoggedState =
    val logger = loggedState.log
    val withLastMove = loggedState.state.updateFlags(_.copy(lastOpponentMove = Some(move)))
    val afterMove = MoveAction(move)(withLastMove)
    val afterDamageTaken = applyPassiveEffects(OnDamageTaken(Opponent))(afterMove)
    if isKnockedOut(afterDamageTaken.opponent.getActive) then
      val afterAttackerKO = applyPassiveEffects(OnKOTaken(Opponent))(afterDamageTaken)
      val flipped = switchSelfOpponent(afterAttackerKO)
      val afterDefenderKO = applyPassiveEffects(OnKOTaken(Self))(flipped)
      switchSelfOpponent(afterDefenderKO)
    else
      afterDamageTaken

  private def executeNonDamageMove(move: Move)(loggedState: LoggedState): LoggedState =
    val afterMove = MoveAction(move)(state)
    val flipped = switchSelfOpponent(afterMove)
    println(s"DEBUG: ${flipped.self.getActive.species.name} status = ${flipped.self.getActive.statusCondition}")
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
