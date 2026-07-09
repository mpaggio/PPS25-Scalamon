package scalamon.domain.actions

import scalamon.domain.pokemon.abilities.AbilityTrigger
import scalamon.domain.pokemon.abilities.Target
import AbilityTrigger.*
import Target.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.domain.moves.AlteredStatus.*

object Items:

  case class Item(
                   name: String,
                   effect: StateTransformer,
                   until: Set[AbilityTrigger] = Set.empty,
                   onCancel: StateTransformer = identity
                 ) extends Action:

    def apply(bs: BattleState): BattleState =
      if bs.self.items.contains(this) then
        val addCancel = addPassiveEffect(t => if until.contains(t) then onCancel else identity)
        val consumeItem = self(items(_ - this))
        (effect andThen addCancel andThen consumeItem)(bs)
      else
        bs

    override def equals(obj: Any): Boolean = obj match
      case item: Item => this.name == item.name
      case _ => false

  val nullItem: Item = Item("null", identity)

  val all: Set[Item] = Set(
    Item("potion", self(active(currentHp(increase(20))))),
    Item("fresh_water", self(active(currentHp(increase(50))))),
    Item("soda_pop", self(active(currentHp(increase(60))))),
    Item("lemonade", self(active(currentHp(increase(80))))),
    Item("hyper_potion", self(active(currentHp(increase(200))))),
    Item("max_potion", self(active(currentHp(increase(5000))))),

    Item("antidote", self(active(removeStatus(Poisoned)))),
    Item("burn_heal", self(active(removeStatus(Burned)))),
    Item("paralyze_heal", self(active(removeStatus(Paralyzed)))),
    Item("awakening", self(active(removeStatus(Sleeping(1))))),

    Item("revive", self(allThat(_.currentHp <= 0)(pk => pk.copy(currentHp = pk.maxHp / 2)))),
    Item("max_revive", self(allThat(_.currentHp <= 0)(pk => pk.copy(currentHp = pk.maxHp)))),

    Item("elixir", self(active(moves(currentPp(increase(10)))))),

    Item(
      "x_attack",
      effect   = self(active(modifyStats(attack(increase(1))))),
      until    = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(modifyStats(attack(decrease(1))))),
    ),
    Item(
      "x_defense",
      effect   = self(active(modifyStats(defense(increase(1))))),
      until    = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(modifyStats(defense(decrease(1))))),
    ),
    Item(
      "x_speed",
      effect   = self(active(modifyStats(speed(increase(1))))),
      until    = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(modifyStats(speed(decrease(1))))),
    ),
    Item(
      "x_precision",
      effect   = self(active(moves(accuracyPercent(_ + 100)))),
      until    = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(moves(accuracyPercent(_ - 100))))
    ),

    Item(
      "calcium",
      self(active(modifyStats(specialAttack(increase(1))))) andThen
        self(active(modifyStats(specialDefense(increase(1)))))
    ),

    Item("carbos", self(active(modifyStats(speed(increase(1))))))
  )