package scalamon.domain.actions

import scalamon.domain.pokemon.abilities.AbilityTrigger
import scalamon.domain.pokemon.abilities.Target
import AbilityTrigger.*
import Target.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.domain.alteredStatus.AlteredStatus.*
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

  val nullItem: Item = Item(
    name = "null",
    description = "None",
    effect = identity
  )

  val allItems: Set[Item] = Set(
    Item(
      name = "potion",
      description = "Heal 20 HP",
      effect = self(active(currentHp(increase(20))))
    ),
    Item(
      name = "fresh_water",
      description = "Heal 50 HP",
      effect = self(active(currentHp(increase(50))))
    ),
    Item(
      name = "soda_pop",
      description = "Heal 60 HP",
      effect = self(active(currentHp(increase(60))))
    ),
    Item(
      name = "lemonade",
      description = "Heal 80 HP",
      effect = self(active(currentHp(increase(80))))
    ),
    Item(
      name = "hyper_potion",
      description = "Heal 200 HP",
      effect = self(active(currentHp(increase(200))))
    ),
    Item(
      name = "max_potion",
      description = "Heal to full HP",
      effect = self(active(currentHp(increase(5000))))
    ),
    Item(
      name = "antidote",
      description = "Cure poison",
      effect = self(active(removeStatus(Poisoned)))
    ),
    Item(
      name = "burn_heal",
      description = "Cure burn",
      effect = self(active(removeStatus(Burned)))
    ),
    Item(
      name = "paralyze_heal",
      description = "Cure paralysis",
      effect = self(active(removeStatus(Paralyzed)))
    ),
    Item(
      name = "awakening",
      description = "Wake up a sleeping pokemon",
      effect = self(active(removeStatus(Sleeping(1))))
    ),
    Item(
      name = "revive",
      description = "Revive all KO Pokémon at half HP",
      effect = self(allThat(_.currentHp <= 0)(pk => pk.copy(currentHp = pk.maxHp / 2)))
    ),
    Item(
      name = "max_revive",
      description = "Revive all KO Pokémon at full HP",
      effect = self(allThat(_.currentHp <= 0)(pk => pk.copy(currentHp = pk.maxHp)))
    ),
    Item(
      name = "elixir",
      description = "Restore 10 PP to active moves",
      effect = self(active(moves(currentPp(increase(10)))))
    ),
    Item(
      name = "x_attack",
      description = "Raise Attack until switch out or KO",
      effect = self(active(modifyStats(attack(increase(1))))),
      until = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(modifyStats(attack(decrease(1)))))
    ),
    Item(
      name = "x_defense",
      description = "Raise Defense until switch out or KO",
      effect = self(active(modifyStats(defense(increase(1))))),
      until = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(modifyStats(defense(decrease(1)))))
    ),
    Item(
      name = "x_speed",
      description = "Raise Speed until switch out or KO",
      effect = self(active(modifyStats(speed(increase(1))))),
      until = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(modifyStats(speed(decrease(1)))))
    ),
    Item(
      name = "x_precision",
      description = "Raise move accuracy until switch out or KO",
      effect = self(active(moves(accuracyPercent(_ + 100)))),
      until = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(moves(accuracyPercent(_ - 100))))
    ),
    Item(
      name = "calcium",
      description = "Raise Special Attack and Special Defense",
      effect = self(active(modifyStats(specialAttack(increase(1))))) andThen self(active(modifyStats(specialDefense(increase(1)))))
    ),
    Item(
      name = "carbos",
      description = "Raise Speed",
      effect = self(active(modifyStats(speed(increase(1)))))
    )
  )