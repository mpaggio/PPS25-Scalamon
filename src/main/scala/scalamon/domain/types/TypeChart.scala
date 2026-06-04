package scalamon.domain.types

object TypeChart:
  import Type.*
  import TypeEffectiveness.*

  private val nonNeutralMatchUps: Map[(Type, Type), TypeEffectiveness] = Map(
    (Fire, Grass) -> SuperEffective,
    (Fire, Water) -> NotVeryEffective,
    (Fire, Fire) -> NotVeryEffective,
    (Water, Fire) -> SuperEffective,
    (Water, Grass) -> NotVeryEffective,
    (Water, Water) -> NotVeryEffective,
    (Grass, Water) -> SuperEffective,
    (Grass, Fire) -> NotVeryEffective,
    (Grass, Grass) -> NotVeryEffective,
    (Grass, Poison) -> NotVeryEffective,
    (Electric, Water) -> SuperEffective,
    (Electric, Grass) -> NotVeryEffective,
    (Electric, Electric) -> NotVeryEffective,
    (Psychic, Poison) -> SuperEffective,
    (Psychic, Psychic) -> NotVeryEffective,
    (Poison, Grass) -> SuperEffective,
    (Poison, Poison) -> NotVeryEffective
  )

  def effectiveness(attacking: Type, defending: Type): TypeEffectiveness =
    nonNeutralMatchUps.getOrElse((attacking, defending), Neutral)
    