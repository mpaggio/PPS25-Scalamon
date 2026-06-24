package scalamon.domain.pokemon.abilities

/**
 * All the moments of the battle when a passive ability can be triggered.
 */
enum AbilityTrigger:
  case OnTurnStart
  case OnTurnEnd
  case OnSwitchIn
  case OnSwitchOut
  case OnDamageTaken
  case OnKODealt
  case OnDamageDealt