package scalamon.domain.pokemon.abilities

/**
 * Manages the ability slots for a Pokémon.
 * @param primary the primary ability of the Pokémon, which is mandatory.
 * @param secondary an optional secondary ability that a Pokémon may have.
 * @param hidden an optional hidden ability that a Pokémon may have, which is mutually exclusive with the secondary ability.
 * @throws IllegalArgumentException if both a secondary and a hidden ability are assigned simultaneously.
 */
case class AbilitySlot(
  primary: Ability,
  secondary: Option[Ability] = None,
  hidden: Option[Ability] = None
):
  require(!(secondary.isDefined && hidden.isDefined), "A Pokemon cannot have both a secondary and a hidden ability at the same time")