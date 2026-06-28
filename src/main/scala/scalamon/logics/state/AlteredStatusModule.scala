package scalamon.logics.state

import scalamon.domain.moves.AlteredStatus
import scalamon.domain.moves.AlteredStatus.*
import scalamon.domain.moves.AlteredStatusUtility.*
import scalamon.domain.moves.Accuracy.*
import scalamon.domain.moves.Accuracy.given
import scalamon.logics.state.StateTransformerModuleImpl.StateTransformer

object AlteredStatusModule:

  extension (status: AlteredStatus)
    def canMove: Boolean = status match
      case Sleeping(_) => false
      case Frozen => accuracyFromPercent(freezeThawingChance).test
      case Paralyzed => !accuracyFromPercent(paralysisFailureChance).test
      case _ => true

    def isSelfHitting: Boolean = status match
      case Confused(_) => accuracyFromPercent(confusionSelfHitChance).test
      case _ => false

    def applyCondition: StateTransformer = battleState => status match
      case Burned | Poisoned =>
        val active = battleState.self.getActive
        val damageAmount = active.species.baseStats.hp.toInt / statusDamageDivisor
        battleState self (_ active (_ takeDamage damageAmount))
      case Sleeping(turns) =>
        if turns > 1 then
          battleState self (_ active (pokemonState => pokemonState.copy(status = List(Sleeping(turns - 1)))))
        else battleState self (_ active (_.clearStatusCondition))
      case Confused(turns) =>
        if turns > 1 then
          battleState self (_ active (pokemonState => pokemonState.copy(status = List(Confused(turns - 1)))))
        else battleState self (_ active (_.clearStatusCondition))
      case Charging(turns) =>
        if turns > 1 then
          battleState self (_ active (pokemonState => pokemonState.copy(status = List(Charging(turns - 1)))))
        else battleState self (_ active (_.clearStatusCondition))
      case _ => battleState
