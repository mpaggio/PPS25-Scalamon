package scalamon.domain.types

object TypeChart:
  import Type.*
  import TypeEffectiveness.*

  /**
   * Type effectiveness chart for matchups between attacking and defending types.
   *
   * This object stores all non-neutral interactions and provides a lookup
   * method to compute the resulting effectiveness for a given pair of types.
   */
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

  /**
   * Computes the effectiveness of an attacking type against a defending type.
   *
   * Returns the specific matchup effectiveness when it is explicitly defined
   * in the chart, or `Neutral` otherwise.
   *
   * @param attacking
   * the attacking type
   * @param defending
   * the defending type
   * @return
   * the effectiveness of the matchup
   */
  def effectiveness(attacking: Type, defending: Type): TypeEffectiveness =
    nonNeutralMatchUps.getOrElse((attacking, defending), Neutral)
    