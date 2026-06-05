package scalamon.domain.pokemon

case class AbilitySlot(
  primary: Ability,
  secondary: Option[Ability] = None,
  hidden: Option[Ability] = None
):
  require(primary != null, "Primary ability must be provided")
  require(!(secondary.isDefined && hidden.isDefined), "A Pokemon cannot have both a secondary and a hidden ability at the same time")