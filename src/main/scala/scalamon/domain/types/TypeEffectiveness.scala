package scalamon.domain.types

/**
 * Represents the effectiveness of an attacking type against a defending type.
 *
 * Each value is associated with a damage multiplier that can be used during
 * damage calculation.
 *
 * @param multiplier
 *   the numeric multiplier applied to damage
 */
enum TypeEffectiveness(val multiplier: Double):
  case NoEffect extends TypeEffectiveness(0.0)
  case NotVeryEffective extends TypeEffectiveness(0.5)
  case Neutral extends TypeEffectiveness(1.0)
  case SuperEffective extends TypeEffectiveness(2.0)
  