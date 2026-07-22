package scalamon.domain.pokemon.abilities

/**
 * The target of the ability trigger, either the Pokémon itself or the opponent's one.
 */
enum Target:
  case Self, Opponent

/**
 * All the moments of the battle when a passive ability can be triggered.
 */
enum AbilityTrigger:
  case OnTurnStart
  case OnTurnEnd
  case OnSwitchIn(target: Target)
  case OnSwitchOut(target: Target)
  case OnForcedSwitchOut(target: Target)
  case OnDamageTaken(target: Target)
  case OnKOTaken(target: Target)