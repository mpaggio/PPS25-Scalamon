package scalamon.logics.turns

trait ActionOrderResolver:
  def order(actions: List[ScheduledAction]): List[ScheduledAction]

object ActionOrderResolver:
  val default: ActionOrderResolver = _.sortBy(_.orderingKey)

  extension (action: BattleAction)
    def priority: Int = action match
      case BattleAction.UseMove(_, _, _, _, priority)    => priority
      case BattleAction.SwitchPokemon(_, _, _, priority) => priority

  extension (scheduledAction: ScheduledAction)
    def orderingKey: (Int, Int) =  (-scheduledAction.action.priority, -scheduledAction.speed.value)