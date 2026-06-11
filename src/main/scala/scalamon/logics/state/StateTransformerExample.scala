package scalamon.logics.state

trait StateTransformerModule:
  type BattleState
  type StateTransformer = BattleState => BattleState
  type TransformerFlatMapper = StateTransformer => List[StateTransformer]

object StateTransformerModuleImpl extends StateTransformerModule:
  override type BattleState = BattleStateImpl.BattleState


// Example of how to use the StateTransformerModule to implement abilities and effects in a Pokemon battle system.

import StateTransformerModuleImpl.*
import PokemonStateModuleImpl.*
import scalamon.domain.moves.DamageMove
import scalamon.logics.state.StatsStateModuleImpl.*


enum Origin:
  case Self, Opponent


trait PeriodicEffect

trait PassiveEffect(selector: (StateTransformer, Origin) => Boolean)(mapper: TransformerFlatMapper):
  def apply(battleState: StateTransformer, origin: Origin): List[StateTransformer] =
    if selector(battleState, origin) then mapper(battleState) else List(battleState)

trait Action extends StateTransformer

// Example actions:

case class SwitchAction(newId: PlayerStateModuleImpl.PokemonId) extends Action:
  override def apply(battleState: BattleState): BattleState = battleState self (_ switchActive newId )

case class DamageAction(move: DamageMove) extends Action:   // implicit parameter for damage calculation ??
  override def apply(battleState: BattleState): BattleState =
    val amount = battleState.self.getActive.modifiedStats.attack.toInt * move.power.asInt // example
    // val amount = DamageMoveCalculator(battleState, move)
    battleState opponent (_ active (_ currentHp (_ decrease amount)))

// Example abilities:

enum Ability(selector: (StateTransformer, Origin) => Boolean, mapper: TransformerFlatMapper) extends PassiveEffect(selector)(mapper):

  case ShadowTag extends Ability(
    {
      case (SwitchAction(_), Origin.Opponent) => true
      case _ => false
    },
    _ => Nil
  )

  case MagicGuard extends Ability(
    {
      case (DamageAction(_), Origin.Opponent) => false
      case _ => false
    },
    _ => Nil
  )

  case Regenerator extends Ability(
    {
      case (SwitchAction(_), Origin.Self) => true
      case _ => false
    },
    bs => List(bs, s => s self (_ active (_ currentHp (_ increase 3))))
  )




// Engine example:

object BattleEngine:
  
  private val switchProspective: StateTransformer = s => s.switchUserEnemy

  def resolveTurn(initialState: BattleState, p1Action: List[StateTransformer], p2Action: List[StateTransformer]): BattleState =
    
    // automatically sort by speed and insert switchProspective where needed
    val actionTimeline = p1Action.::(switchProspective) ++ p2Action
    
    // resolve on list using like flatMap
    val actionWithPassives = resolvePassiveEffects(p1Action, initialState)   // Can passive trigger passive??

    actionWithPassives.foldLeft(initialState)((state, effect) => effect(state))   // final fold


  private def resolvePassiveEffects(actions: List[StateTransformer], battleState: BattleState): List[StateTransformer] =

    val passiveAbilities: List[(Origin, Ability)] = List(
      (Origin.Self, Ability.Regenerator),  //  battleState.user.getActive.species.abilitySlot.primary
      (Origin.Opponent, Ability.ShadowTag)  // battleState.enemy.getActive.species.abilitySlot.primary ...
    )

    actions.flatMap(action => action match
      case switchProspective => List(action)
      case _ => passiveAbilities.foldLeft(List(action))((currentActions, entry) =>
        val (origin, ability) = entry // !!
        currentActions.flatMap(act => ability.apply(act, origin))
      )
    )
