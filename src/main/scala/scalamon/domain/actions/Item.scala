package scalamon.domain.actions

import scalamon.domain.pokemon.abilities.AbilityTrigger
import AbilityTrigger.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.logics.log.BattleLogger.{logUseItem, logItemRunsOut, logError}


case class Item(
                 name: String,
                 description: String,
                 effect: StateTransformer,
                 until: Set[AbilityTrigger] = Set.empty,
                 onCancel: StateTransformer = identity
               ) extends Action:

  def apply(bs: BattleState): BattleState =
    if bs.self.items.contains(this) then
      val onTrigger = onCancel andThen removePassiveEffect(name) andThen updateLogs(logItemRunsOut(bs.self, this))
      val addCancel = addPassiveEffect(name, t => if until.contains(t) then onTrigger else identity)
      val consumeItem = self(items(_ - this))
      val logItemUse = updateLogs(logUseItem(bs.self, this))
      (effect andThen addCancel andThen consumeItem andThen logItemUse)(bs)
    else
      updateLogs(logError(s"Item $name not found"))(bs)

  override def equals(obj: Any): Boolean = obj match
    case item: Item => this.name == item.name
    case _ => false
