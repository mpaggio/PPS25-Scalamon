package scalamon.domain.pokemon

case class AbilitySlot(
  primary: Ability,
  secondary: Option[Ability] = None,
  hidden: Option[Ability] = None
):
  require(!(secondary.isDefined && hidden.isDefined), "A Pokemon cannot have both a secondary and a hidden ability at the same time")