package scalamon.domain.actions

import scalamon.domain.pokemon.abilities.AbilityTrigger
import scalamon.domain.pokemon.abilities.Target
import AbilityTrigger.*
import Target.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.domain.moves.AlteredStatus.*
import scalamon.logics.log.BattleLogger
import scalamon.logics.log.BattleLogger.logUseItem

object Items:

  case class Item(
                   name: String,
                   description: String,
                   effect: StateTransformer,
                   until: Set[AbilityTrigger] = Set.empty,
                   onCancel: StateTransformer = identity
                 ) extends Action:

    def apply(bs: BattleState): BattleState =
      if bs.self.items.contains(this) then
        val addCancel = addPassiveEffect(t => if until.contains(t) then onCancel else identity)
        val consumeItem = self(items(_ - this))
        val logItemUse = updateLogs(logUseItem(bs.self, this))
        (effect andThen addCancel andThen consumeItem andThen logItemUse)(bs)
      else
        updateLogs(BattleLogger.logError(s"Item $name not found"))(bs)

    override def equals(obj: Any): Boolean = obj match
      case item: Item => this.name == item.name
      case _ => false

  val nullItem: Item = Item("null", "null", identity)

  val all: Set[Item] = Set(
    Item("potion", "Heal 20 HP", self(active(currentHp(increase(20))))),
    Item("fresh_water", "Heal 50 HP", self(active(currentHp(increase(50))))),
    Item("soda_pop", "Heal 60 HP", self(active(currentHp(increase(60))))),
    Item("lemonade", "Heal 80 HP", self(active(currentHp(increase(80))))),
    Item("hyper_potion", "Heal 200 HP", self(active(currentHp(increase(200))))),
    Item("max_potion", "Heal to full HP", self(active(currentHp(increase(5000))))),

    Item("antidote", "Cure poison", self(active(removeStatus(Poisoned)))),
    Item("burn_heal", "Cure burn", self(active(removeStatus(Burned)))),
    Item("paralyze_heal", "Cure paralysis", self(active(removeStatus(Paralyzed)))),
    Item("awakening", "Cure sleeping", self(active(removeStatus(Sleeping(1))))),

    Item("revive", "Revive all KO Pokemon at half HP", self(allThat(_.currentHp <= 0)(pk => pk.copy(currentHp = pk.maxHp / 2)))),
    Item("max_revive", "Revive all KO Pokemon at full HP", self(allThat(_.currentHp <= 0)(pk => pk.copy(currentHp = pk.maxHp)))),

    Item("elixir", "Restore 10 PP to active moves", self(active(moves(currentPp(increase(10)))))),

    Item(
      "x_attack",
      "Raise attack until switch out or KO",
      effect   = self(active(modifyStats(attack(increase(1))))),
      until    = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(modifyStats(attack(decrease(1))))),
    ),
    Item(
      "x_defense",
      "Raise defense until switch out or KO",
      effect   = self(active(modifyStats(defense(increase(1))))),
      until    = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(modifyStats(defense(decrease(1))))),
    ),
    Item(
      "x_speed",
      "Raise speed until switch out or KO",
      effect   = self(active(modifyStats(speed(increase(1))))),
      until    = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(modifyStats(speed(decrease(1))))),
    ),
    Item(
      "x_precision",
      "Raise move accuracy until switch out or KO",
      effect   = self(active(moves(accuracyPercent(_ + 100)))),
      until    = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(moves(accuracyPercent(_ - 100))))
    ),

    Item(
      "calcium",
      "Raise special attack and special defense",
      self(active(modifyStats(specialAttack(increase(1))))) andThen
        self(active(modifyStats(specialDefense(increase(1)))))
    ),

    Item("carbos", "Raise speed", self(active(modifyStats(speed(increase(1))))))
  )