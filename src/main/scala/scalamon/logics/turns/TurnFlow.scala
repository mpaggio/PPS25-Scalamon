package scalamon.logics.turns

import scalamon.logics.turns.BattleAction.{SwitchPokemon, UseMove}

/**
 * The two actions selected by the trainers for the current turn.
 *
 * @param first
 *   the first chosen action
 * @param second
 *   the second chosen action
 */
final case class TurnChoices(first: BattleAction, second: BattleAction)

/**
 * Resolves the execution plan for a turn starting from the players' choices.
 */
trait TurnFlow:
  /**
   * Starts a turn by scheduling the chosen actions and resolving their execution order.
   *
   * @param choices
   * the actions selected for the turn
   * @param speedOf
   * a function returning the speed of a Pokémon reference
   * @return
   * the execution plan for the turn
   */
  def startTurn(choices: TurnChoices, speedOf: PokemonRef => Speed): TurnExecutionPlan

object TurnFlow:
  /**
   * Creates a [[TurnFlow]] backed by the given action order resolver.
   *
   * @param resolver
   * the resolver used to determine action order
   * @return
   * a turn flow instance
   */
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