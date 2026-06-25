package scalamon.domain.pokemon.abilities

import scalamon.domain.moves.*
import scalamon.domain.moves.AlteredStatus.*
import scalamon.domain.pokemon.abilities.Ability.*
import scalamon.domain.pokemon.abilities.AbilityDSL.{AbilityBook, OnTrigger}
import scalamon.domain.pokemon.abilities.AbilityTrigger.*
import scalamon.domain.types.Type.*
import scalamon.domain.weather.Weather
import scalamon.domain.weather.Weather.*
import scalamon.logics.state.BattleStateImpl.BattleState
import scalamon.logics.state.StatsStateModuleImpl.multiply

import scala.language.postfixOps
import scala.util.Random

object MyAbilityBook:

  private def healSelf(fraction: Int)(state: BattleState): BattleState =
    val maxHp = state.self.getActive.maxHp
    state self (_ active (_ heal (maxHp/fraction)))

  private def damageSelf(fraction: Int)(state: BattleState): BattleState =
    val maxHp = state.self.getActive.maxHp
    state self (_ active (_ takeDamage (maxHp / fraction)))

  private def reduceOpponentAttack(state: BattleState, fraction: Double): BattleState =
    state opponent (_ active (_ modifyStats (_ attack (_ multiply (1 - fraction)))))

  private def foreWarnLog(state: BattleState): BattleState =
    val opponentMoves = state.opponent.getActive.moves.keys
    println(s"[Forewarn] The opponent has the following moves: ${opponentMoves.mkString(", ")}")
    state


  private val book: Map[Ability, List[AbilityDefinition]] = AbilityBook(

    // FIRE

    OnTrigger(OnDamageDealt) define Blaze asNoop,

    OnTrigger(OnTurnEnd) define SolarScales as { state =>
      if state.weather == Weather.HeavySunlight then healSelf(16)(state) else state
    },

    OnTrigger(OnTurnEnd) define SolarPower as { state =>
      if state.weather == Weather.HeavySunlight then damageSelf(16)(state) else state
    },

    OnTrigger(OnSwitchIn) define Drought as { state =>
      if state.weather == Weather.ClearSky then state.setWeather(Weather.HeavySunlight) else state
    },

    OnTrigger(OnDamageTaken) define FlashFire as { state =>
      if state.flags.lastOpponentMove.exists(_.moveType == Fire) then
        println(s"[FlashFire] ${state.self.getActive.species.name} is now immune to Fire moves and gains a boost!")
        state.updateFlags(_.copy(selfFlashFireActive = true))
      else state
    },

    OnTrigger(OnDamageDealt) define DroughtAura asNoop,

    OnTrigger(OnDamageTaken) define FlameBody as{ state =>
      if Random.nextDouble() < 0.30 then
        println(s"[FlameBody] ${state.opponent.getActive.species.name} is burned!")
        state opponent (_ active (_ setStatus Burned))
      else state

    },

    OnTrigger(OnTurnStart) define RunAway asNoop,

    OnTrigger(OnDamageDealt) define Guts asNoop,

    // WATER

    OnTrigger(OnDamageDealt) define Torrent asNoop,

    OnTrigger(OnTurnStart) define EarlyBird asNoop,

    OnTrigger(OnTurnEnd) define RainDish as { state =>
      if state.weather == Weather.Rain then healSelf(16)(state) else state
    },

    OnTrigger(OnDamageTaken) define WaterAbsorb asNoop,

    OnTrigger(OnTurnStart) define Hydration as { state =>
      if state.weather == Weather.Rain then state self (_ active (_ clearStatusCondition)) else state 
    },

    OnTrigger(OnSwitchIn) define Intimidate as { state =>
      println(s"{Intimidate] ${state.self.getActive.species.name} intimidates the opponent! -10% Attack")
      reduceOpponentAttack(state, 0.1)
    },

    OnTrigger(OnKODealt) define Moxie as { state =>
      println(s"[Moxie] ${state.self.getActive.species.name} gains +10% Attack after KO!")
      state self (_ active (_ modifyStats (_ attack (_ multiply 1.1))))
    },

    // GRASS

    OnTrigger(OnDamageDealt) define Overgrow asNoop,

    OnTrigger(OnTurnStart) define Chlorophyll as { state =>
      if !state.flags.weatherSuppressed && state.weather == HeavySunlight then
        println(s"[Chlorophyll] ${state.self.getActive.species.name} doubles its Speed in Heavy Sunlight!")
        state self (_ active (_ modifyStats (_ speed (_ multiply 2.0))))
      else state
    },

    OnTrigger(OnDamageTaken) define ThickFat asNoop,

    OnTrigger(OnTurnStart) define Gluttony asNoop,

    OnTrigger(OnDamageTaken) define EffectSpore as { state =>
      if Random.nextDouble() < 0.30 then
        val status = Random.nextInt(3) match
          case 0 => Paralyzed
          case 1 => Poisoned
          case _ => Sleeping
        println(s"[EffectSpore]) ${state.opponent.getActive.species.name} is ${status.toString}!")
        state opponent (_ active (_ setStatus status))
      else state
    },

    OnTrigger(OnSwitchOut) define Regenerator as { state =>
      val maxHp = state.self.getActive.maxHp
      println(s"[Regenerator] ${state.self.getActive.species.name} regenerates 1/3 of its max HP while leaving!")
      state self (_ active (_ heal (maxHp / 3)))
    },

    // ELECTRIC

    OnTrigger(OnDamageDealt) define Static as { state =>
      if Random.nextDouble() < 0.30 then
        println(s"[Static] ${state.opponent.getActive.species.name} is paralyzed!")
        state opponent (_ active (_ setStatus Paralyzed))
      else state
    },

    OnTrigger(OnDamageTaken) define LightningRodLite asNoop,

    OnTrigger(OnDamageTaken) define LightningRod asNoop,

    OnTrigger(OnTurnStart) define Ability.SurgeSurfer as { state =>
      if !state.flags.weatherSuppressed && state.weather == Weather.Thunderstorm then
        state self (_ active (_ modifyStats(_ speed (_ multiply(2.0)))))
      else state
    },

    OnTrigger(OnKODealt) define Aftermath asNoop,

    OnTrigger(OnDamageTaken) define VitalSpirit asNoop,

    OnTrigger(OnDamageTaken) define VoltAbsorb asNoop,

    OnTrigger(OnTurnStart) define QuickFeet as { state =>
      val self = state.self.getActive
      if self.statusCondition.isDefined then
        state self (_ active (_ modifyStats(_ speed (_ multiply(1.5)))))
      else state
    },

    // PSICO

    OnTrigger(OnDamageTaken) define Synchronize as { state =>
      state.self.getActive.statusCondition match
        case Some(s) => state opponent (_ active (_ setStatus s))
        case None => state
    },

    OnTrigger(OnTurnEnd) define MagicGuard asNoop,

    OnTrigger(OnDamageTaken) define Insomnia asNoop,

    OnTrigger(OnSwitchIn) define Forewarn as { state =>
      foreWarnLog(state)
    },

    OnTrigger(OnTurnEnd) define DrySkin as { state =>
      state.weather match
        case Weather.Rain => healSelf(16)(state)
        case Weather.ClearSky => damageSelf(16)(state)
        case _ => state
    },

    OnTrigger(OnDamageTaken) define Pressure asNoop,

    OnTrigger(OnSwitchIn) define Unnerve as { state =>
      state.updateFlags(_.copy(opponentItemSuppressed = true))
    },

    OnTrigger(OnSwitchIn) define CloudNine as { state =>
      println(s"[CloudNine] ${state.self.getActive.species.name} suppresses weather effects!")
      state.updateFlags(_.copy(weatherSuppressed = true))
    },

    OnTrigger(OnTurnStart) define SwiftSwim as { state =>
      if !state.flags.weatherSuppressed && state.weather == Weather.Rain then
        state self (_ active (_ modifyStats(_ speed (_ multiply(2.0)))))
      else state
    },

    // POISON

    OnTrigger(OnTurnStart) define ShedSkin as { state =>
      if Random.nextDouble() < 0.30 then
        println(s"[ShedSkin] ${state.self.getActive.species.name} recovers from status conditions!")
        state self (_ active (_ clearStatusCondition))
      else state
    },

    OnTrigger(OnDamageTaken) define StickyHold asNoop,

    OnTrigger(OnDamageTaken) define PoisonTouch as { state =>
      if Random.nextDouble() < 0.30 then
        println(s"[PoisonTouch] ${state.opponent.getActive.species.name} is poisoned!")
        state opponent (_ active (_ setStatus Poisoned))
      else state
    },

    OnTrigger(OnDamageTaken) define Levitate asNoop,

    OnTrigger(OnDamageTaken) define CursedBody asNoop,

    OnTrigger(OnSwitchIn) define ShadowTag as { state =>
      state.updateFlags(_.copy(opponentSwitchBlocked = true))
    },

    OnTrigger(OnDamageTaken) define LiquidOoze asNoop,
  )

  // PUBLIC API

  /**
   * Returns all the definitions of a given ability, filtered by the trigger.
   * @param ability the ability to look up
   * @param trigger the trigger to filter by
   * @return a list of ability definitions that match the ability and trigger
   */
  private def lookupByTrigger(ability: Ability, trigger: AbilityTrigger): List[AbilityDefinition] =
    book.getOrElse(ability, List.empty).filter(_.trigger == trigger)

  /**
   * Returns all the abilities in a given ability slot.
   * @param slot the ability slot to look up
   * @return a list of abilities in the slot
   */
  def allSlots(slot: AbilitySlot): List[Ability] =
    List(Some(slot.primary), slot.secondary, slot.hidden).flatten

  /**
   * Executes all the effects of a triggered ability for a given Pokémon on the state.
   * The caller is responsible for the orientation self/opponent before invoking this function.
   * @param trigger the trigger that caused the ability to activate
   * @param slot the ability slot of the Pokémon whose ability is being triggered
   * @param state the current battle state before the ability effects are applied
   * @return the new battle state after all the ability effects have been applied
   */
  def runTrigger(trigger: AbilityTrigger, slot: AbilitySlot)(state: BattleState): BattleState =
    allSlots(slot)
      .flatMap(lookupByTrigger(_, trigger))
      .foldLeft(state)((s, define) => define.effect.run(s))