package scalamon.logics.turns

import scalamon.logics.turns.BattleAction.{SwitchPokemon, UseMove}

final case class TurnChoises(first: BattleAction, second: BattleAction)

final case class Turn(actions: List[ScheduledAction])

trait TurnFlow:
  def startTurn(choises: TurnChoises, speedOf: PokemonRef => Speed): Turn

object TurnFlow:
  def apply(resolver: ActionOrderResolver): TurnFlow = new TurnFlow:
    override def startTurn(choises: TurnChoises, speedOf: PokemonRef => Speed): Turn =
      val scheduledAction = List(choises.first, choises.second).map(schedule(_, speedOf))
      Turn(resolver.order(scheduledAction))

  private def schedule(action: BattleAction, speedOf: PokemonRef => Speed): ScheduledAction =
    ScheduledAction(action = action, speed = speedOf(userOf(action)))

  private def userOf(action: BattleAction): PokemonRef = action match
    case UseMove(_, user, _, _, _) => user
    case SwitchPokemon(_, from, _, _) => from