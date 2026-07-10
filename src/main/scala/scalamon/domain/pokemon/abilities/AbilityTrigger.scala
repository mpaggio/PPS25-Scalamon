package scalamon.domain.pokemon.abilities

enum Target:
  case Self, Opponent

/**
 * All the moments of the battle when a passive ability can be triggered.
 */
enum AbilityTrigger:
  case OnTurnStart
  case OnTurnEnd
  case OnSwitchIn(target: Target) // Se target = Self, si attiva perchè se stesso è entrato
  case OnSwitchOut(target: Target) // Se target = Self, si attiva perchè se stesso è uscito
  case OnDamageTaken(target: Target) // Se target = Self, si attiva perchè se stesso ha subito danno
  case OnKOTaken(target: Target) // Se target = Self, si attiva perchè se stesso è andato KO