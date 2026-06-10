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
  case User, Enemy
  def opponent: Origin = this match
    case User  => Enemy
    case Enemy => User

enum From:
  case Self, Opponent


trait PeriodicEffect

trait PassiveEffect(selector: (StateTransformer, From) => Boolean)(mapper: TransformerFlatMapper):
  def apply(battleState: StateTransformer, from: From): List[StateTransformer] =
    if selector(battleState, from) then mapper(battleState) else List(battleState)

trait Action extends StateTransformer

// Example actions:

case class SwitchAction(newId: PlayerStateModuleImpl.PokemonId) extends Action:
  override def apply(battleState: BattleState): BattleState = battleState user (_ switchActive newId )

case class DamageAction(move: DamageMove) extends Action:   // implicit parameter for damage calculation ??
  override def apply(battleState: BattleState): BattleState =
    val amount = battleState.user.getActive.modifiedStats.attack.toInt * move.power.asInt // example
    // val amount = DamageMoveCalculator(battleState, move)
    battleState enemy (_ active (_ currentHp (_ decrease amount)))

// Example abilities:

enum Ability(selector: (StateTransformer, From) => Boolean, mapper: TransformerFlatMapper) extends PassiveEffect(selector)(mapper):

  case ShadowTag extends Ability(
    {
      case (SwitchAction(_), From.Opponent) => true
      case _ => false
    },
    _ => Nil
  )

  case MagicGuard extends Ability(
    {
      case (DamageAction(_), From.Opponent) => false
      case _ => false
    },
    _ => Nil
  )

  case Regenerator extends Ability(
    {
      case (SwitchAction(_), From.Self) => true
      case _ => false
    },
    bs => List(bs, s => s user (_ active (_ currentHp (_ increase 3))))
  )




// Engine example:


case class TargetedInstantEffect(actor: Origin, action: StateTransformer)


def resolvePassiveEffects(timeline: List[TargetedInstantEffect], battleState: BattleState): List[TargetedInstantEffect] =

  val activeAbilities: List[(Origin, Ability)] = List(
    (Origin.User, Ability.Regenerator),  //  battleState.user.getActive.species.abilitySlot.primary
    (Origin.Enemy, Ability.ShadowTag)  // battleState.enemy.getActive.species.abilitySlot.primary
  )

  timeline.flatMap:
    case TargetedInstantEffect(actor, action) =>
      val finalActions = activeAbilities.foldLeft(List(action))((currentActions, entry) =>
        val (abilityOwner, ability) = entry

        val relation = if actor == abilityOwner then From.Self else From.Opponent

        currentActions.flatMap(act => ability.apply(act, relation))
      )

      finalActions.map(act => TargetedInstantEffect(actor, act))
