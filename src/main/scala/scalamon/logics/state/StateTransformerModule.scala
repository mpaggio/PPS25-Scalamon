package scalamon.logics.state

trait StateTransformerModule:
  type BattleState
  type StateTransformer = BattleState => BattleState
  type TransformerFlatMapper = StateTransformer => List[StateTransformer]

object StateTransformerModuleImpl extends StateTransformerModule:
  override type BattleState = BattleStateImpl.BattleState




import StateTransformerModuleImpl.*

trait InstantEffect extends StateTransformer

case class SwitchAction(newId: PlayerStateModuleImpl.PokemonId) extends InstantEffect:
  override def apply(battleState: BattleState): BattleState = battleState user (_ switchActive newId )

trait PassiveEffect(selector: StateTransformer => Boolean)(mapper: TransformerFlatMapper) extends TransformerFlatMapper:
  override def apply(battleState: StateTransformer): List[StateTransformer] =
    if selector(battleState) then mapper(battleState) else List(battleState)

enum Ability(selector: StateTransformer => Boolean)(mapper: TransformerFlatMapper) extends PassiveEffect(selector)(mapper):
  case ShadowTag extends Ability({ case SwitchAction(_) => true })(b => List())

trait AlteredStatus

trait PeriodicEffect