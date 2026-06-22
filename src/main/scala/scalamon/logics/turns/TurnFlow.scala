package scalamon.logics.turns

import scalamon.logics.turns.BattleAction.{SwitchPokemon, UseMove}

final case class TurnChoices(first: BattleAction, second: BattleAction)

trait TurnFlow:
  def startTurn(choices: TurnChoices, speedOf: PokemonRef => Speed): TurnExecutionPlan

object TurnFlow:
  def apply(resolver: ActionOrderResolver): TurnFlow = new TurnFlow:
    override def startTurn(choices: TurnChoices, speedOf: PokemonRef => Speed): TurnExecutionPlan =
      val scheduledAction = List(choices.first, choices.second).map(_.scheduleWith(speedOf))
      TurnExecutionPlan(resolver.order(scheduledAction))

    extension (action: BattleAction)
      def user: PokemonRef = action match
        case UseMove(_, user, _, _, _) => user
        case SwitchPokemon(_, from, _, _) => from

      def scheduleWith(speedOf: PokemonRef => Speed): ScheduledAction =
        ScheduledAction(action = action, speed = speedOf(action.user))