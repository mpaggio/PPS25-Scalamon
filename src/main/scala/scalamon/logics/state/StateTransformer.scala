package scalamon.logics.state

trait StateTransformerModule:
  type BattleState
  type StateTransformer = BattleState => BattleState
  type TransformerFlatMapper = StateTransformer => List[StateTransformer]

object StateTransformerModuleImpl extends StateTransformerModule:
  override type BattleState = BattleStateImpl.BattleState

  export MoveStateModuleImpl.*
  export StatsStateModuleImpl.*
  export PokemonStateModuleImpl.{StatsState => _, MoveState => _, *}
  export PlayerStateModuleImpl.{PokemonState => _, *}
  export BattleStateImpl.{PlayerState => _, BattleState => _, *}

/*
// Example of how to use the StateTransformerModule to implement abilities and effects in a Pokemon battle system.

import StateTransformerModuleImpl.*
import PokemonStateModuleImpl.*
import scalamon.domain.moves.DamageMove
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.StatsStateModuleImpl.*


enum Origin:
  case Self, Opponent


trait PeriodicEffect

trait PassiveEffect(selector: (StateTransformer, Origin) => Boolean)(mapper: TransformerFlatMapper):
  def apply(battleState: StateTransformer, origin: Origin): List[StateTransformer] =
    if selector(battleState, origin) then mapper(battleState) else List(battleState)

trait Action extends StateTransformer
// type Action = List[StateTransformer]

// Example actions:

case class SwitchAction(newId: PlayerStateModuleImpl.PokemonId) extends Action:
  override def apply(battleState: BattleState): BattleState = battleState self (_ switchActive newId )

case class DamageAction(move: DamageMove) extends Action:   // implicit parameter for damage calculation ??
  override def apply(battleState: BattleState): BattleState =
    val amount = battleState.self.getActive.modifiedStats.attack.toInt * move.power.asInt // example
    // val amount = DamageMoveCalculator(battleState, move)
    battleState opponent (_ active (_ currentHp (_ decrease amount)))

// Example abilities:

class Ability(selector: (StateTransformer, Origin) => Boolean, mapper: TransformerFlatMapper) extends PassiveEffect(selector)(mapper)

val shadowTag = Ability(
  {
    case (SwitchAction(_), Origin.Opponent) => true
    case _ => false
  },
  _ => Nil
)

val regenerator = Ability(
  {
    case (SwitchAction(_), Origin.Self) => true
    case _ => false
  },
  bs => List(bs, s => s self (_ active (_ currentHp (_ increase 3))))
)


class AlteredStatus(effect: PassiveEffect, duration: Int, update: Int => Int):
  def apply(actions: List[StateTransformer]): List[StateTransformer] =
    val updatedDuration = update(duration)
    if updatedDuration > 0 then
      actions.flatMap(action => effect.apply(action, Origin.Self))
    else actions
    

// Engine example:

object BattleEngine:

  private val switchProspective: StateTransformer = s => s.switchSelfOpponent

  def resolveTurn(initialState: BattleState, p1Action: List[StateTransformer], p2Action: List[StateTransformer]): BattleState =

    // automatically sort by speed and insert switchProspective where needed
    val actionTimeline = p1Action.::(switchProspective) ++ p2Action

    // resolve on list using like flatMap
    val actionWithPassives = resolvePassiveEffects(p1Action, initialState)   // Can passive trigger passive??

    actionWithPassives.foldLeft(initialState)((state, effect) => effect(state))   // final fold


  private def resolvePassiveEffects(actions: List[StateTransformer], battleState: BattleState): List[StateTransformer] =

    val passiveAbilities: List[(Origin, Ability)] = List(
      (Origin.Self, shadowTag),  //  battleState.self.getActive.species.abilitySlot.primary
      (Origin.Opponent, regenerator)  // battleState.oppotent.getActive.species.abilitySlot.primary ...
    )

    actions.flatMap(action => action match
      case switchProspective => List(action)
      case _ => passiveAbilities.foldLeft(List(action))((currentActions, entry) =>
        val (origin, ability) = entry // !!
        currentActions.flatMap(act => ability.apply(act, origin))
      )
    )
*/