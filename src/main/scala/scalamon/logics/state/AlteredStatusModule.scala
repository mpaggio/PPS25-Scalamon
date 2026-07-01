package scalamon.logics.state

import scalamon.domain.moves.AlteredStatus
import scalamon.domain.moves.AlteredStatus.*
import scalamon.domain.moves.AlteredStatusUtility.*
import scalamon.domain.moves.Accuracy.*
import scalamon.domain.moves.Accuracy.given
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.StateTransformerModuleImpl.StateTransformer

object AlteredStatusModule:

  extension (status: AlteredStatus)
    def canMove: Boolean = status match
      case Sleeping(_) | Charging(_) => false
      case Frozen => accuracyFromPercent(freezeThawingChance).test
      case Paralyzed => !accuracyFromPercent(paralysisFailureChance).test
      case _ => true

    def isSelfHitting: Boolean = status match
      case Confused(_) => accuracyFromPercent(confusionSelfHitChance).test
      case _ => false

    def applyCondition: StateTransformer = battleState => status match
      case Burned | Poisoned =>
        if battleState.flags.selfMagicGuardActive then battleState
        else
          val a = battleState.self.getActive
          val damageAmount = a.species.baseStats.hp.toInt / statusDamageDivisor
          self(active(takeDamage(damageAmount)))(battleState)
      case Sleeping(turns) =>
        if turns > 1 then
          self(active(pokemonState => pokemonState.copy(status = List(Sleeping(turns - 1)))))(battleState)
        else self(active(_.clearStatusCondition))(battleState)
      case Confused(turns) =>
        if turns > 1 then
          self(active(pokemonState => pokemonState.copy(status = List(Confused(turns - 1)))))(battleState)
        else self(active(_.clearStatusCondition))(battleState)
      case Charging(turns) =>
        if turns > 1 then
          self(active(pokemonState => pokemonState.copy(status = List(Charging(turns - 1)))))(battleState)
        else self(active(_.clearStatusCondition))(battleState)
      case _ => battleState