package scalamon.domain.actions

import scalamon.domain.pokemon.abilities.AbilityTrigger
import scalamon.domain.pokemon.abilities.Target
import AbilityTrigger.*
import Target.*
import scalamon.logics.state.StateTransformerModuleImpl.*
import scalamon.domain.moves.AlteredStatus.*

object Items:

  case class Item(
                   effect: StateTransformer,
                   until: Set[AbilityTrigger] = Set.empty,
                   onCancel: StateTransformer = identity
                 ) extends Action:
    def apply(bs: BattleState): BattleState =
      addPassiveEffect(t => if until.contains(t) then onCancel else identity)(effect(bs))

  val all: Map[String, Item] = Map(
    "potion"       -> Item(self(active(currentHp(increase(20))))),
    "fresh_water"  -> Item(self(active(currentHp(increase(50))))),
    "soda_pop"     -> Item(self(active(currentHp(increase(60))))),
    "lemonade"     -> Item(self(active(currentHp(increase(80))))),
    "hyper_potion" -> Item(self(active(currentHp(increase(200))))),
    "max_potion"   -> Item(self(active(currentHp(increase(5000))))),

    "antidote"      -> Item(self(active(removeStatus(Poisoned)))),
    "burn_heal"     -> Item(self(active(removeStatus(Burned)))),
    "paralyze_heal" -> Item(self(active(removeStatus(Paralyzed)))),
    "awakening"     -> Item(self(active(removeStatus(Sleeping(1))))),

    "revive"     -> Item(self(allThat(_.currentHp <= 0)(pk => pk.copy(currentHp = pk.maxHp / 2)))),
    "max_revive" -> Item(self(allThat(_.currentHp <= 0)(pk => pk.copy(currentHp = pk.maxHp)))),

    "elixir" -> Item(self(active(moves(currentPp(increase(10)))))),

    "x_attack"  -> Item(
      effect   = self(active(modifyStats(attack(increase(1))))),
      until    = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(modifyStats(attack(decrease(1))))),
    ),
    "x_defense" -> Item(
      effect   = self(active(modifyStats(defense(increase(1))))),
      until    = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(modifyStats(defense(decrease(1))))),
    ),
    "x_speed"   -> Item(
      effect   = self(active(modifyStats(speed(increase(1))))),
      until    = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(modifyStats(speed(decrease(1))))),
    ),
    "x_precision" -> Item(
      effect   = self(active(moves(accuracyPercent(_ + 100)))),
      until    = Set(OnSwitchOut(Self), OnKOTaken(Self)),
      onCancel = self(active(moves(accuracyPercent(_ - 100))))
    ),

    "calcium" -> Item(
      self(active(modifyStats(specialAttack(increase(1))))) andThen
        self(active(modifyStats(specialDefense(increase(1)))))
    ),

    "carbos" -> Item(self(active(modifyStats(speed(increase(1))))))
  )