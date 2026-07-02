package scalamon.domain.actions

import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.PokemonId

case class SwitchAction(pokemonId: PokemonId) extends Action:
  def apply(bs: BattleState): BattleState = self(switchActive(pokemonId))(bs)