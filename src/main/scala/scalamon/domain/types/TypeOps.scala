package scalamon.domain.types

import scalamon.domain.types.TypeEffectiveness.*

extension (attacking: Type)

  def effectivenessAgainst(defending: Type): TypeEffectiveness =
    TypeChart.effectiveness(attacking, defending)

  def multiplierAgainst(defending: Type): Double =
    attacking.effectivenessAgainst(defending).multiplier

  def isSuperEffectiveAgainst(defending: Type): Boolean =
    attacking.effectivenessAgainst(defending) == SuperEffective

  def isNotVeryEffectiveAgainst(defending: Type): Boolean =
    attacking.effectivenessAgainst(defending) == NotVeryEffective
    
  def hasNoEffectAgainst(defending: Type): Boolean =
    attacking.effectivenessAgainst(defending) == NoEffect
    