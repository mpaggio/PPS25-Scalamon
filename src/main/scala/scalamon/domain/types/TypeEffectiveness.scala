package scalamon.domain.types

enum TypeEffectiveness(val multiplier: Double):
  case NoEffect extends TypeEffectiveness(0.0)
  case NotVeryEffective extends TypeEffectiveness(0.5)
  case Neutral extends TypeEffectiveness(1.0)
  case SuperEffective extends TypeEffectiveness(2.0)