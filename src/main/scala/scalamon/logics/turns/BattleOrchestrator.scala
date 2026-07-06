package scalamon.logics.turns

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
import scalamon.logics.turns.ActionOrder.*

final class BattleOrchestrator(using DamagePolicy):
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
    case Player1First => List(
      executeAction(choices.player1Action),
      switchSelfOpponent,
      executeAction(choices.player2Action),
      switchSelfOpponent
    )
    case Player2First => List(
      switchSelfOpponent,
      executeAction(choices.player2Action),
      switchSelfOpponent,
      executeAction(choices.player1Action)
    )

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

  private def executeMove(moveRef: MoveRef)(state: BattleState): BattleState =
    val activePokemon = state.self.getActive
    resolveMove(moveRef) match
      case Some(move) if !hasPpFor(activePokemon, move) =>
        println(s" ${activePokemon.species.name} non ha abbastanza PP per user ${moveRef.value}!")
        state
      case Some(move: DamageMove) if !canMove(activePokemon) =>
        println(s" ${activePokemon.species.name} non puo' muoversi a causa del suo stato!")
        state
      case Some(move: DamageMove) if isSelfHitting(activePokemon) => executeSelfHit(move)(state)
      case Some(move: DamageMove) => executeDamageMove(move)(state)
      case Some(move: NonDamagingMove) => executeNonDamageMove(move)(state)
      case None => state

  private def resolveMove(moveRef: MoveRef): Option[Move] =
    MoveDatabase.allMoves.findByName(moveRef.value)

  private def hasPpFor(pokemon: PokemonState, move: Move): Boolean =
    pokemon.moveState(move.name).currentPp > 0

  private def canMove(pokemon: PokemonState): Boolean =
    pokemon.statusCondition.forall(_.canMove)

  private def isSelfHitting(pokemon: PokemonState): Boolean =
    pokemon.statusCondition.exists(_.isSelfHitting)

  private def executeSelfHit(move: DamageMove)(state: BattleState): BattleState =
    val active = state.self.getActive
    println(s" ${active.species.name} si colpisce da solo usando ${move.name}!")
    MoveAction(move, Target.Self)(state)

  private def executeDamageMove(move: DamageMove)(state: BattleState): BattleState =
    val withLastMove = state.updateFlags(_.copy(lastOpponentMove = Some(move)))
    val afterMove = MoveAction(move)(withLastMove)
    val afterDamageTaken = applyPassiveEffects(OnDamageTaken(Opponent))(afterMove)
    val defenderIsKnockedOut = isKnockedOut(afterDamageTaken.opponent.getActive)
    if defenderIsKnockedOut then
      val afterAttackerKO = applyPassiveEffects(OnKOTaken(Opponent))(afterDamageTaken)
      val flipped = switchSelfOpponent(afterAttackerKO)
      val afterDefenderKO = applyPassiveEffects(OnKOTaken(Self))(flipped)
      switchSelfOpponent(afterDefenderKO)
    else
      afterDamageTaken

  private def executeNonDamageMove(move: Move)(state: BattleState): BattleState =
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
