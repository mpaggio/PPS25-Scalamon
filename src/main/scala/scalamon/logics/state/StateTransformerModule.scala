package scalamon.logics.state

trait StateTransformerModule:
  type BattleState
  type StateTransformer = BattleState => BattleState
  type TransformerFlatMapper = StateTransformer => List[StateTransformer]

object StateTransformerModuleImpl extends StateTransformerModule:
  override type BattleState = BattleStateImpl.BattleState




import StateTransformerModuleImpl.*
import PokemonStateModuleImpl.*
import scalamon.domain.moves.DamageMove
import scalamon.logics.state.StatsStateModuleImpl.*

type InstantEffect = StateTransformer

trait Action extends InstantEffect

case class SwitchAction(newId: PlayerStateModuleImpl.PokemonId) extends Action:
  override def apply(battleState: BattleState): BattleState = battleState user (_ switchActive newId )

case class DamageAction(move: DamageMove) extends Action:   // implicit parameter for damage calculation ??
  override def apply(battleState: BattleState): BattleState =
    val amount = battleState.user.getActive.modifiedStats.attack.toInt * move.power.asInt // example
    // val damageAmount = DamageMoveCalculator(battleState, move)
    battleState enemy (_ active (_ currentHp (_ decrease amount)))

trait PassiveEffect(selector: StateTransformer => Boolean)(mapper: TransformerFlatMapper) extends TransformerFlatMapper:
  override def apply(battleState: StateTransformer): List[StateTransformer] =
    if selector(battleState) then mapper(battleState) else List(battleState)

enum Ability(selector: StateTransformer => Boolean)(mapper: TransformerFlatMapper) extends PassiveEffect(selector)(mapper):
  case ShadowTag extends Ability({ case SwitchAction(_) => true })(bs => List())
  case MagicGuard extends Ability({ case DamageAction(_) => false })(bs => List())
  case Regenerator extends Ability({ case SwitchAction(_) => true })(bs => List(bs, s => s user (_ active (_ currentHp (_ increase 3)))))


trait AlteredStatus

trait PeriodicEffect