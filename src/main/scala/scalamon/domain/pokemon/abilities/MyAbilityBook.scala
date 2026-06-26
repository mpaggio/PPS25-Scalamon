package scalamon.domain.pokemon.abilities

import scalamon.domain.moves.*
import scalamon.domain.moves.AlteredStatus.*
import scalamon.domain.moves.DamageMoveCategory.Physical
import scalamon.domain.pokemon.abilities.Ability.*
import scalamon.domain.pokemon.abilities.AbilityDSL.{AbilityBook, OnTrigger}
import scalamon.domain.pokemon.abilities.AbilityTrigger.*
import scalamon.domain.types.Type.*
import scalamon.domain.weather.Weather
import scalamon.domain.weather.Weather.*
import scalamon.logics.state.BattleStateImpl.BattleState
import scalamon.logics.state.StatsStateModuleImpl.multiply
import scalamon.domain.moves.AlteredStatusUtility.*

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

    OnTrigger(OnDamageDealt) define Blaze as { state =>
      println(s"[Blaze] ${state.self.getActive.species.name} is using Blaze! Fire moves are boosted!")
      state
    },

    OnTrigger(OnTurnEnd) define SolarScales as { state =>
      println(s"[SolarScales] ${state.self.getActive.species.name} with Heavy Sunlight heals 1/16 of its max HP and its special moves boosts + 30%!")
      if state.weather == Weather.HeavySunlight then healSelf(16)(state) else state
    },

    OnTrigger(OnTurnEnd) define SolarPower as { state =>
      println(s"[SolarPower] ${state.self.getActive.species.name} with Heavy Sunlight takes 1/16 of its max HP as damage!")
      if state.weather == Weather.HeavySunlight then damageSelf(16)(state) else state
    },

    OnTrigger(OnSwitchIn) define Drought as { state =>
      println(s"[Drought] ${state.self.getActive.species.name} sets Heavy Sunlight!")
      if state.weather == Weather.ClearSky then state.setWeather(Weather.HeavySunlight) else state
    },

    OnTrigger(OnDamageTaken) define FlashFire as { state =>
      if state.flags.lastOpponentMove.exists(_.moveType == Fire) then
        println(s"[FlashFire] ${state.self.getActive.species.name} is now immune to Fire moves and gains a boost!")
        state.updateFlags(_.copy(selfFlashFireActive = true))
      else state
    },

    OnTrigger(OnDamageDealt) define DroughtAura as { state =>
      println(s"[DroughtAura] ${state.self.getActive.species.name} boosts Fire moves by 10%!")
      state
    },

    OnTrigger(OnDamageTaken) define FlameBody as{ state =>
      if Random.nextDouble() < 0.30 then
        println(s"[FlameBody] ${state.opponent.getActive.species.name} is burned!")
        state opponent (_ active (_ addStatus Burned))
      else state
    },

    OnTrigger(OnTurnStart) define RunAway as { state =>
      val self = state.self.getActive
      val baseSpeed = self.species.baseStats.speed.toInt
      val modifiedSpeed = self.modifiedStats.speed
      if modifiedSpeed < baseSpeed then
        println(s"[RunAway] ${self.species.name}'s Speed cannot be reduced!")
        state self (_ active (_ modifyStats (_ speed (_ => baseSpeed))))
      else state
    },

    OnTrigger(OnDamageDealt) define Guts as { state =>
      println(s"[Guts] ${state.self.getActive.species.name} boosts its Physical Attack by 30% when it has a status condition!")
      state
    },

    // WATER

    OnTrigger(OnDamageDealt) define Torrent as { state =>
      println(s"[Torrent] ${state.self.getActive.species.name} is using Torrent! Water moves are boosted!")
      state
    },

    OnTrigger(OnTurnEnd) define RainDish as { state =>
      println(s"[RainDish] ${state.self.getActive.species.name} with Rain heals 1/16 of its max HP!")
      if state.weather == Weather.Rain then healSelf(16)(state) else state
    },

    OnTrigger(OnDamageTaken) define WaterAbsorb as { state =>
      state.flags.lastOpponentMove match
        case Some(move) if move.moveType == Water =>
          val maxHp = state.self.getActive.maxHp
          println(s"[WaterAbsorb] ${state.self.getActive.species.name} absorbs Water moves and heals 1/4 of its max HP!")
          state self (_ active (_ heal (maxHp / 4)))
        case _ => state
    },

    OnTrigger(OnTurnStart) define Hydration as { state =>
      println(s"[Hydration] ${state.self.getActive.species.name} with Rain clears status conditions!")
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

    OnTrigger(OnDamageDealt) define Overgrow as { state =>
      println(s"[Overgrow] ${state.self.getActive.species.name} is using Overgrow! Grass moves are boosted!")
      state
    },

    OnTrigger(OnTurnStart) define Chlorophyll as { state =>
      if !state.flags.weatherSuppressed && state.weather == HeavySunlight then
        println(s"[Chlorophyll] ${state.self.getActive.species.name} doubles its Speed in Heavy Sunlight!")
        state self (_ active (_ modifyStats (_ speed (_ multiply 2.0))))
      else state
    },

    OnTrigger(OnDamageTaken) define ThickFat as { state =>
      println(s"[ThickFat] ${state.self.getActive.species.name} takes 50% less damage from Fire moves!")
      state
    },

    OnTrigger(OnDamageTaken) define EffectSpore as { state =>
      if Random.nextDouble() < 0.30 then
        val status = Random.nextInt(3) match
          case 0 => Paralyzed
          case 1 => Poisoned
          case _ => Sleeping(getSleepTurns)
        println(s"[EffectSpore]) ${state.opponent.getActive.species.name} is ${status.toString}!")
        state opponent (_ active (_ addStatus status))
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
        state opponent (_ active (_ addStatus Paralyzed))
      else state
    },

    OnTrigger(OnDamageTaken) define LightningRodLite as { state =>
      state.flags.lastOpponentMove match
        case Some(move) if move.moveType == Electric =>
          println(s"[LightningRodLite] ${state.self.getActive.species.name} is protected from Electric Moves!")
          val maxHp = state.self.getActive.maxHp
          state self (_ active (_ heal (maxHp / 8)))
        case _ => state
    },

    OnTrigger(OnDamageTaken) define LightningRod as { state =>
      state.flags.lastOpponentMove match
        case Some(move) if move.moveType == Electric =>
          println(s"[LightningRod] ${state.self.getActive.species.name} draws Electric moves and boosts Special Attack!")
          state self (_ active (_ modifyStats(_ specialAttack (_ multiply 1.5))))
        case _ => state
    },

    OnTrigger(OnTurnStart) define Ability.SurgeSurfer as { state =>
      if !state.flags.weatherSuppressed && state.weather == Weather.Thunderstorm then
        println(s"[SurgeSurfer] ${state.self.getActive.species.name} doubles its Speed in Thunderstorm!")
        state self (_ active (_ modifyStats(_ speed (_ multiply 2.0))))
      else state
    },

    OnTrigger(OnKODealt) define Aftermath as { state =>
      println(s"[Aftermath] ${state.opponent.getActive.species.name} takes damage after KO!")
      state opponent (_ active (_ takeDamage (state.opponent.getActive.maxHp / 8)))
    },

    OnTrigger(OnDamageTaken) define VoltAbsorb as { state =>
      state.flags.lastOpponentMove match
        case Some(move) if move.moveType == Electric =>
          val maxHp = state.self.getActive.maxHp
          println(s"[VoltAbsorb] ${state.self.getActive.species.name} absorbs Electric moves and heals 1/4 of its max HP!")
          state self (_ active (_ heal (maxHp / 4)))
        case _ => state
    },

    OnTrigger(OnTurnStart) define QuickFeet as { state =>
      val self = state.self.getActive
      if self.statusCondition.isDefined then
        println(s"[QuickFeet] ${self.species.name} boosts its Speed by 50% while having a status condition!")
        state self (_ active (_ modifyStats(_ speed (_ multiply 1.5))))
      else state
    },

    // PSICO

    OnTrigger(OnDamageTaken) define Synchronize as { state =>
      println(s"[Synchronize] if ${state.self.getActive.species.name} obtains a Status condition, it applies it to the opponent too!")
      state.self.getActive.statusCondition match
        case Some(s) => state opponent (_ active (_ addStatus s))
        case None => state
    },

    OnTrigger(OnTurnStart) define MagicGuard as { state =>
      println(s"[MagicGuard] ${state.self.getActive.species.name} is immune to indirect damage of the altered status!")
      state.updateFlags(_.copy(selfMagicGuardActive = true))
    },

    OnTrigger(OnDamageTaken) define Insomnia as { state =>
      println(s"[VitalSpirit] ${state.self.getActive.species.name} is immune to Sleep!")
      state
    },

    OnTrigger(OnSwitchIn) define Forewarn as { state =>
      foreWarnLog(state)
    },

    OnTrigger(OnTurnEnd) define DrySkin as { state =>
      println(s"[DrySkin] ${state.self.getActive.species.name}'s stats are affected by the current weather!")
      state.weather match
        case Weather.Rain => healSelf(16)(state)
        case Weather.ClearSky => damageSelf(16)(state)
        case _ => state
    },

    OnTrigger(OnDamageTaken) define Pressure as { state =>
      state.flags.lastOpponentMove match
        case Some(move) =>
          println(s"[Pressure] ${state.opponent.getActive.species.name}'s ${move.name} loses 1 additional PP due to Pressure!")
          state opponent (_ active (active => active.updateMove(move.name)(ms => ms.decreasePpBy(1))))
        case None => state
    },

    OnTrigger(OnSwitchIn) define CloudNine as { state =>
      println(s"[CloudNine] ${state.self.getActive.species.name} suppresses weather effects!")
      state.updateFlags(_.copy(weatherSuppressed = true))
    },

    OnTrigger(OnTurnStart) define SwiftSwim as { state =>
      println(s"[SwiftSwim] ${state.self.getActive.species.name} doubles its Speed in Rain!")
      if !state.flags.weatherSuppressed && state.weather == Weather.Rain then
        state self (_ active (_ modifyStats(_ speed (_ multiply 2.0))))
      else state
    },

    // POISON

    OnTrigger(OnTurnStart) define ShedSkin as { state =>
      if Random.nextDouble() < 0.30 then
        println(s"[ShedSkin] ${state.self.getActive.species.name} recovers from status conditions!")
        state self (_ active (_ clearStatusCondition))
      else state
    },

    OnTrigger(OnDamageTaken) define PoisonTouch as { state =>
      if Random.nextDouble() < 0.30 then
        println(s"[PoisonTouch] ${state.opponent.getActive.species.name} is poisoned!")
        state opponent (_ active (_ addStatus Poisoned))
      else state
    },

    OnTrigger(OnDamageTaken) define Levitate as { state =>
      state.flags.lastOpponentMove match
        case Some(move) if move.moveType == Physical =>
          println(s"[Levitate] ${state.self.getActive.species.name} is immune to Physical moves!")
          state
        case _ => state
    },

    OnTrigger(OnDamageTaken) define CursedBody as { state =>
      if Random.nextDouble() < 0.30 && state.flags.lastOpponentMove.isDefined then
        val moveName = state.flags.lastOpponentMove.get.name
        println(s"[CursedBody] ${state.opponent.getActive.species.name}'s $moveName is disabled!")
        state opponent (_ active (active => active.updateMove(moveName)(ms => ms.copy(currentPp = 0))))
      else state
    },

    OnTrigger(OnSwitchIn) define ShadowTag as { state =>
      println(s"[ShadowTag] ${state.self.getActive.species.name} prevents the opponent from switching the active Pokemon!")
      state.updateFlags(_.copy(opponentSwitchBlocked = true))
    },

    OnTrigger(OnDamageTaken) define LiquidOoze as { state =>
      if Random.nextDouble() < 0.30 then
        val attacker = state.opponent.getActive
        val maxHp = attacker.maxHp
        println(s"[LiquidOoze] ${attacker.species.name} takes damage from the Ooze!")
        state opponent (_ active (_ takeDamage (maxHp / 8)))
      else state
    },
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