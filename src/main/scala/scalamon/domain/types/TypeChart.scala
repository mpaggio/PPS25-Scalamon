package scalamon.domain.types

object TypeChart:
  import Type.*
  import TypeEffectiveness.*

  private object TypeSyntax:
    extension (attacking: Type)
      infix def strongAgainst(defending: Type): ((Type, Type), TypeEffectiveness) =
        (attacking, defending) -> SuperEffective

      infix def weakAgainst(defending: Type): ((Type, Type), TypeEffectiveness) =
        (attacking, defending) -> NotVeryEffective

      infix def noEffectAgainst(defending: Type): ((Type, Type), TypeEffectiveness) =
        (attacking, defending) -> NoEffect

  import TypeSyntax.*

  private val nonNeutralMatchUps: Map[(Type, Type), TypeEffectiveness] = Map(
    Fire strongAgainst Grass,
    Fire weakAgainst Water,
    Fire weakAgainst Fire,
    Water strongAgainst Fire,
    Water weakAgainst Grass,
    Water weakAgainst Water,
    Grass strongAgainst Water,
    Grass weakAgainst Fire,
    Grass weakAgainst Grass,
    Grass weakAgainst Poison,
    Electric strongAgainst Water,
    Electric weakAgainst Grass,
    Electric weakAgainst Electric,
    Psychic strongAgainst Poison,
    Psychic weakAgainst Psychic,
    Poison strongAgainst Grass,
    Poison weakAgainst Poison
  )

  def effectiveness(attacking: Type, defending: Type): TypeEffectiveness =
    nonNeutralMatchUps.getOrElse((attacking, defending), Neutral)
    