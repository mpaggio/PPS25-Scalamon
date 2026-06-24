package scalamon.logics.turns

/**
 * Execution plan for a turn, containing the actions in their resolved order.
 *
 * @param orderedActions
 *   the actions to execute, already sorted by scheduling order
 */
final case class TurnExecutionPlan(orderedActions: List[ScheduledAction])