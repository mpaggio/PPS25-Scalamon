package scalamon.domain.actions

import scalamon.logics.log.BattleLogger
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.PokemonId

case class SwitchAction(pokemonId: PokemonId) extends Action:
  def apply(bs: BattleState): BattleState =
    val previousActive = bs.self.getActive
    val switched = self(switchActive(pokemonId))(bs)
    updateLogs(BattleLogger.logSwitchPokemon(previousActive, switched.self.getActive))(switched)