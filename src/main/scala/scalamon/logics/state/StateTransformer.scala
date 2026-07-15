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
