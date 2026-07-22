package scalamon.domain.types

import scalamon.domain.types.TypeEffectiveness.*

extension (attacking: Type)

  /**
   * Computes the effectiveness of this attacking type against a defending type.
   *
   * @param defending
   *   the defending type
   * @return
   *   the resulting type effectiveness
   */
  def effectivenessAgainst(defending: Type): TypeEffectiveness =
    TypeChart.effectiveness(attacking, defending)

  /**
   * Computes the damage multiplier of this attacking type against a defending type.
   *
   * @param defending
   * the defending type
   * @return
   * the numeric effectiveness multiplier
   */
  def multiplierAgainst(defending: Type): Double =
    attacking.effectivenessAgainst(defending).multiplier

  /**
   * Checks whether this attacking type is super effective against a defending type.
   *
   * @param defending
   * the defending type
   * @return
   * `true` if the matchup is super effective, `false` otherwise
   */
  def isSuperEffectiveAgainst(defending: Type): Boolean =
    attacking.effectivenessAgainst(defending) == SuperEffective

  /**
   * Checks whether this attacking type is not very effective against a defending type.
   *
   * @param defending
   * the defending type
   * @return
   * `true` if the matchup is not very effective, `false` otherwise
   */
  def isNotVeryEffectiveAgainst(defending: Type): Boolean =
    attacking.effectivenessAgainst(defending) == NotVeryEffective

  /**
   * Checks whether this attacking type is neutral against a defending type.
   *
   * @param defending
   * the defending type
   * @return
   * `true` if the matchup is neutral, `false` otherwise
   */
  def isNeutralAgainst(defending: Type): Boolean =
    attacking.effectivenessAgainst(defending) == Neutral

  /**
   * Checks whether this attacking type has no effect against a defending type.
   *
   * @param defending
   * the defending type
   * @return
   * `true` if the matchup has no effect, `false` otherwise
   */
  def hasNoEffectAgainst(defending: Type): Boolean =
    attacking.effectivenessAgainst(defending) == NoEffect
