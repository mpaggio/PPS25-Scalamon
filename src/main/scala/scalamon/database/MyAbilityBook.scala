package scalamon.database

import scalamon.domain.alteredStatus.AlteredStatus.*
import scalamon.domain.alteredStatus.AlteredStatusUtility.*
import scalamon.domain.moves.*
import scalamon.domain.pokemon.abilities.Ability.*
import scalamon.domain.pokemon.abilities.AbilityDSL.{AbilityBook, OnTrigger}
import scalamon.domain.pokemon.abilities.AbilityTrigger.*
import scalamon.domain.pokemon.abilities.Target.*
import scalamon.domain.pokemon.abilities.{Ability, AbilityDefinition, AbilitySlot, AbilityTrigger}
import scalamon.domain.types.Type.*
import scalamon.domain.weather.Weather
import scalamon.domain.weather.Weather.*
import scalamon.logics.log.BattleLogger
import scalamon.logics.state.BattleStateImpl.{opponent, self}
import scalamon.logics.state.StateTransformerModuleImpl.*

import scala.language.postfixOps
import scala.util.Random

/**
 * Contains the definitions of all abilities and their effects in the battle system.
 * Each ability is associated with specific triggers and effects that modify the battle state.
 */
object MyAbilityBook:

  /**
   * Function to heal the active Pokémon by a fraction of its maximum HP.
   * @param fraction fraction of max HP to heal (e.g., 30 means heal 1/30 of max HP)
   * @param state the current battle state
   * @return the new battle state after healing
   */
  private def healSelf(fraction: Int)(state: BattleState): BattleState =
    val maxHp = state.self.getActive.maxHp
    self(active(heal(maxHp/fraction)))(state)

  /**
   * Function to damage the active Pokémon by a fraction of its maximum HP.
   * @param fraction fraction of max HP to take as damage
   * @param state the current battle state
   * @return the new battle state after taking damage
   */
  private def damageSelf(fraction: Int)(state: BattleState): BattleState =
    val maxHp = state.self.getActive.maxHp
    self(active(takeDamage(maxHp / fraction)))(state)

  /**
   * Function to reduce the opponent's active Pokémon's attack stat by a given fraction.
   * @param state the current battle state
   * @param fraction the fraction by which to reduce the opponent's attack stat
   * @return the new battle state after reducing the opponent's attack stat
   */
  private def reduceOpponentAttack(state: BattleState, fraction: Double): BattleState =
    opponent(active(modifyStats(attack(multiply(1 - fraction)))))(state)

  /**
   * Function to log the opponent's moves for when the Forewarn ability is triggered.
   * @param state the current battle state
   * @return the same battle state, unchanged
   */
  private def foreWarnLog(state: BattleState): BattleState =
    val opponentMoves = state.opponent.getActive.moves.keys
    log(s"[Forewarn] The opponent's ${state.opponent.getActive.species.name} has the following moves: ${opponentMoves.mkString(", ")}")(state)

  /**
   * Map of the abilities to their corresponding definitions and effects, organized by ability type.
   */
  private val book: Map[Ability, List[AbilityDefinition]] = AbilityBook(

    // FIRE ABILITIES

    OnTrigger(OnDamageTaken(Opponent)) define Blaze as { state =>
      state
    },

    OnTrigger(OnTurnEnd) define SolarScales as { state =>
      if state.weather == Weather.HeavySunlight then
        val loggedState = log(s"[SolarScales] ${state.self.getActive.species.name} with Heavy Sunlight heals 1/30 of its max HP and its special moves boosts + 30%!")(state)
        healSelf(30)(loggedState)
      else state
    },

    OnTrigger(OnTurnEnd) define SolarPower as { state =>
      if state.weather == Weather.HeavySunlight then
        val loggedState = log(s"[SolarPower] ${state.self.getActive.species.name} with Heavy Sunlight takes 1/30 of its max HP as damage!")(state)
        damageSelf(30)(loggedState)
      else state
    },

    OnTrigger(OnSwitchIn(Self)) define Drought as { state =>
      if state.weather == Weather.ClearSky then
        val loggedState = log(s"[Drought] ${state.self.getActive.species.name} changes ClearSky with Heavy Sunlight!")(state)
        setWeather(Weather.HeavySunlight)(loggedState)
      else state
    },

    OnTrigger(OnDamageTaken(Self)) define FlashFire as { state =>
      if !state.self.flags.flashFireActive &&
        state.opponent.flags.lastMove.exists(move =>
          move.moveType == Fire &&
            state.opponent.getActive.currentHp > 0
        )
      then
        val loggedState = log(s"[FlashFire] ${state.self.getActive.species.name} is now immune to Fire moves and gains a boost!")(state)
        self(updateFlags(_.copy(flashFireActive = true)))(loggedState)
      else state
    },

    OnTrigger(OnDamageTaken(Opponent)) define DroughtAura as { state =>
      state
    },

    OnTrigger(OnDamageTaken(Self)) define FlameBody as{ state =>
      if Random.nextDouble() < 0.30 && state.opponent.getActive.statusCondition.isEmpty then
        val loggedState = log(s"[FlameBody] ${state.opponent.getActive.species.name} is burned " +
          s"by ${state.self.getActive.species.name}'s FlameBody ability!")(state)
        opponent(active(addStatus(Burned)))(loggedState)
      else state
    },

    OnTrigger(OnTurnStart) define RunAway as { state =>
      val selfActive = state.self.getActive
      val baseSpeed = selfActive.species.baseStats.speed.toInt
      val modifiedSpeed = selfActive.modifiedStats.speed
      if modifiedSpeed < baseSpeed then
        val loggedState = log(s"[RunAway] ${selfActive.species.name}'s Speed cannot be reduced!")(state)
        self(active(modifyStats(speed(_ => baseSpeed))))(loggedState)
      else state
    },

    OnTrigger(OnDamageTaken(Opponent)) define Guts as { state =>
      state
    },

    // WATER ABILITIES

    OnTrigger(OnDamageTaken(Opponent)) define Torrent as { state =>
      state
    },

    OnTrigger(OnTurnEnd) define RainDish as { state =>
      if state.weather == Weather.Rain then
        val loggedState = log(s"[RainDish] ${state.self.getActive.species.name} with Rain heals 1/30 of its max HP!")(state)
        healSelf(30)(loggedState)
      else state
    },

    OnTrigger(OnDamageTaken(Self)) define WaterAbsorb as { state =>
      state.opponent.flags.lastMove match
        case Some(move) if move.moveType == Water =>
          val maxHp = state.self.getActive.maxHp
          val loggedState = log(s"[WaterAbsorb] ${state.self.getActive.species.name} absorbs Water moves and heals 1/4 of its max HP!")(state)
          self(active(heal(maxHp / 4)))(loggedState)
        case _ => state
    },

    OnTrigger(OnTurnStart) define Hydration as { state =>
      if state.weather == Weather.Rain && state.self.getActive.statusCondition.isDefined then
        val loggedState = log(s"[Hydration] ${state.self.getActive.species.name} with Rain clears status conditions!")(state)
        self(active(clearStatusCondition))(loggedState)
      else state
    },

    OnTrigger(OnSwitchIn(Self)) define Intimidate as { state =>
      val loggedState = log(s"[Intimidate] ${state.self.getActive.species.name} intimidates the opponent! -10% Attack")(state)
      reduceOpponentAttack(loggedState, 0.1)
    },

    OnTrigger(OnKOTaken(Opponent)) define Moxie as { state =>
      val loggedState = log(s"[Moxie] ${state.self.getActive.species.name} gains +10% Attack after KO!")(state)
      self(active(modifyStats(attack(multiply(1.1)))))(loggedState)
    },

    // GRASS ABILITIES

    OnTrigger(OnDamageTaken(Opponent)) define Overgrow as { state =>
      state
    },

    OnTrigger(OnTurnStart) define Chlorophyll as { state =>
      if !state.self.flags.weatherSuppressed && state.weather == HeavySunlight then
        val loggedState = log(s"[Chlorophyll] ${state.self.getActive.species.name} doubles its Speed in Heavy Sunlight!")(state)
        self(active(modifyStats(speed(multiply(2.0)))))(loggedState)
      else state
    },

    OnTrigger(OnDamageTaken(Self)) define ThickFat as { state =>
      state
    },

    OnTrigger(OnDamageTaken(Self)) define EffectSpore as { state =>
      if Random.nextDouble() < 0.30 && state.opponent.getActive.statusCondition.isEmpty then
        val status = Random.nextInt(3) match
          case 0 => Paralyzed
          case 1 => Poisoned
          case _ => Sleeping(getSleepTurns)
        val loggedState = log(s"[${state.self.getActive.species.name}'s EffectSpore] :${state.opponent.getActive.species.name} is ${status.toString}!")(state)
        opponent(active(addStatus(status)))(loggedState)
      else state
    },

    OnTrigger(OnSwitchOut(Self)) define Regenerator as { state =>
      val maxHp = state.self.getActive.maxHp
      val loggedState = log(s"[Regenerator] ${state.self.getActive.species.name} regenerates 1/3 of its max HP while leaving!")(state)
      self(active(heal(maxHp / 3)))(loggedState)
    },

    // ELECTRIC ABILITIES

    OnTrigger(OnDamageTaken(Opponent)) define Static as { state =>
      if Random.nextDouble() < 0.30 && state.opponent.getActive.statusCondition.isEmpty then
        val loggedState = log(s"[Static] ${state.opponent.getActive.species.name} is paralyzed " +
          s"due to ${state.self.getActive.species.name} Static ability!")(state)
        opponent(active(addStatus(Paralyzed)))(loggedState)
      else state
    },

    OnTrigger(OnDamageTaken(Self)) define LightningRodLite as { state =>
      state.opponent.flags.lastMove match
        case Some(move) if move.moveType == Electric =>
          val loggedState = log(s"[LightningRodLite] ${state.self.getActive.species.name} is protected from Electric Moves!")(state)
          val maxHp = state.self.getActive.maxHp
          self(active(heal(maxHp / 8)))(loggedState)
        case _ => state
    },

    OnTrigger(OnDamageTaken(Self)) define LightningRod as { state =>
      state.opponent.flags.lastMove match
        case Some(move) if move.moveType == Electric =>
          val loggedState = log(s"[LightningRod] ${state.self.getActive.species.name} draws Electric moves and boosts Special Attack!")(state)
          self(active(modifyStats(specialAttack(multiply(1.5)))))(loggedState)
        case _ => state
    },

    OnTrigger(OnTurnStart) define Ability.SurgeSurfer as { state =>
      if !state.self.flags.weatherSuppressed && state.weather == Weather.Thunderstorm then
        val loggedState = log(s"[SurgeSurfer] ${state.self.getActive.species.name} doubles its Speed in Thunderstorm!")(state)
        self(active(modifyStats(speed(multiply(2.0)))))(loggedState)
      else state
    },

    OnTrigger(OnKOTaken(Self)) define Aftermath as { state =>
      val loggedState = log(s"[Aftermath] ${state.opponent.getActive.species.name} takes damage after KO!")(state)
      opponent(active(takeDamage(state.opponent.getActive.maxHp / 8)))(loggedState)
    },

    OnTrigger(OnDamageTaken(Self)) define VoltAbsorb as { state =>
      state.opponent.flags.lastMove match
        case Some(move) if move.moveType == Electric =>
          val maxHp = state.self.getActive.maxHp
          val loggedState = log(s"[VoltAbsorb] ${state.self.getActive.species.name} absorbs Electric moves and heals 1/4 of its max HP!")(state)
          self(active(heal(maxHp / 4)))(loggedState)
        case _ => state
    },

    OnTrigger(OnTurnStart) define QuickFeet as { state =>
      val selfActive = state.self.getActive
      if selfActive.statusCondition.isDefined then
        val loggedState = log(s"[QuickFeet] ${selfActive.species.name} boosts its Speed by 50% while having a status condition!")(state)
        self(active(modifyStats(speed(multiply(1.5)))))(loggedState)
      else state
    },

    // PSYCHIC ABILITIES

    OnTrigger(OnDamageTaken(Self)) define Synchronize as { state =>
      state.self.getActive.statusCondition match
        case Some(s) =>
          s match
            case Burned | Paralyzed | Poisoned =>
              val loggedState =
                log(s"[Synchronize] ${state.self.getActive.species.name} passes $s to ${state.opponent.getActive.species.name}!")(state)
              opponent(active(addStatus(s)))(loggedState)
            case _ => state
        case None => state
    },

    OnTrigger(OnTurnEnd) define MagicGuard as { state =>
      val updatedState = self(updateFlags(_.copy(magicGuardActive = true)))(state)
      updatedState.self.getActive.statusCondition match
        case Some(Burned | Paralyzed | Poisoned | Frozen | Sleeping(_)) =>
          val loggedState = log(s"[MagicGuard] ${updatedState.self.getActive.species.name} is immune to indirect damage of the altered status!")(state)
          self(updateFlags(_.copy(magicGuardActive = true)))(loggedState)
        case _ => updatedState
    },

    OnTrigger(OnSwitchIn(Self)) define Forewarn as { state =>
      foreWarnLog(state)
    },

    OnTrigger(OnTurnEnd) define DrySkin as { state =>
      state.weather match
        case Weather.Rain =>
          val loggedState = log(s"[DrySkin] ${state.self.getActive.species.name}'s stats are affected by the current weather!")(state)
          healSelf(30)(loggedState)
        case Weather.HeavySunlight =>
          val loggedState = log(s"[DrySkin] ${state.self.getActive.species.name}'s stats are affected by the current weather!")(state)
          damageSelf(30)(loggedState)
        case _ => state
    },

    OnTrigger(OnDamageTaken(Self)) define Pressure as { state =>
      state.opponent.flags.lastMove match
        case Some(move) =>
          val loggedState = log(s"[Pressure] ${state.opponent.getActive.species.name}'s ${move.name} loses 1 additional PP due " +
            s"to ${state.self.getActive.species.name}'s Pressure ability!")(state)
          opponent(active(updateMove(move.name)(decreasePpBy(1))))(loggedState)
        case None => state
    },

    OnTrigger(OnSwitchIn(Self)) define CloudNine as { state =>
      val loggedState = log(s"[CloudNine] ${state.self.getActive.species.name} suppresses weather effects!")(state)
      val firstUpdated = self(updateFlags(_.copy(weatherSuppressed = true)))(loggedState)
      opponent(updateFlags(_.copy(weatherSuppressed = true)))(firstUpdated)
    },

    OnTrigger(OnTurnStart) define SwiftSwim as { state =>
      if !state.self.flags.weatherSuppressed && state.weather == Weather.Rain then
        val loggedState = log(s"[SwiftSwim] ${state.self.getActive.species.name} doubles its Speed in Rain!")(state)
        self(active(modifyStats(speed(multiply(2.0)))))(loggedState)
      else state
    },

    // POISON ABILITIES

    OnTrigger(OnTurnEnd) define ShedSkin as { state =>
      if Random.nextDouble() < 0.30  && state.self.getActive.statusCondition.isDefined then
        val loggedState = log(s"[ShedSkin] ${state.self.getActive.species.name} recovers from status conditions!")(state)
        self(active(clearStatusCondition))(loggedState)
      else state
    },

    OnTrigger(OnDamageTaken(Opponent)) define PoisonTouch as { state =>
      if Random.nextDouble() < 0.30 && state.opponent.getActive.statusCondition.isEmpty then
        val loggedState = log(s"[PoisonTouch] ${state.opponent.getActive.species.name} is poisoned " +
          s"by ${state.self.getActive.species.name}'s PoisonTouch ability!")(state)
        opponent(active(addStatus(Poisoned)))(loggedState)
      else state
    },

    OnTrigger(OnDamageTaken(Self)) define Levitate as { state =>
      state
    },

    OnTrigger(OnDamageTaken(Self)) define CursedBody as { state =>
      if Random.nextDouble() < 0.30 && state.opponent.flags.lastMove.isDefined then
        val moveName = state.opponent.flags.lastMove.get.name
        val loggedState = log(s"[CursedBody] ${state.opponent.getActive.species.name}'s $moveName is disabled!")(state)
        opponent(active(updateMove(moveName)(ms => ms.copy(currentPp = 0))))(loggedState)
      else state
    },

    OnTrigger(OnSwitchIn(Self)) define ShadowTag as { state =>
      val loggedState = log(s"[ShadowTag] ${state.self.getActive.species.name} prevents the opponent from switching the active Pokemon!")(state)
      opponent(updateFlags(_.copy(isSwitchBlocked = true)))(loggedState)
    },

    OnTrigger(OnDamageTaken(Self)) define LiquidOoze as { state =>
      if Random.nextDouble() < 0.30 then
        val attacker = state.opponent.getActive
        val maxHp = attacker.maxHp
        val loggedState = log(s"[LiquidOoze] ${attacker.species.name} takes damage from the Ooze!")(state)
        opponent(active(takeDamage(maxHp / 8)))(loggedState)
      else state
    },
  )

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
  private def allSlots(slot: AbilitySlot): List[Ability] =
    List(Some(slot.primary), slot.secondary, slot.hidden).flatten

  /**
   * Logs a message to the battle log and updates the state accordingly.
   * @param message the message to log
   * @return the updated state transformer
   */
  private def log(message: String): StateTransformer = updateLogs(BattleLogger.logMessage(message))

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