package scalamon.logics.log

import scalamon.domain.actions.Item
import scalamon.domain.moves.Move
import scalamon.domain.alteredStatus.AlteredStatus
import scalamon.logics.state.BattleStateImpl.PlayerState
import scalamon.logics.state.PokemonStateModuleImpl.PokemonState
import scalamon.domain.weather.Weather
import scalamon.domain.types.Type

/**
 * Functional logging interface for battle events.
 *
 * This module models a battle log as an immutable value that can be updated
 * by appending messages describing relevant events during turn execution.
 */
trait BattleLogger:
  /**
   * Concrete logger representation used by this module.
   */
  type BattleLogger

  /**
   * Adds an error message to the battle log.
   *
   * @param message
   *   the error message to record
   * @param battleLogger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logError(message: String)(battleLogger: BattleLogger): BattleLogger

  /**
   * Adds a generic message to the battle log.
   *
   * @param message
   *   the message to record
   * @param battleLogger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logMessage(message: String)(battleLogger: BattleLogger): BattleLogger

  /**
   * Logs the usage of an item by a player.
   *
   * @param player
   *   the player using the item
   * @param itemName
   *   the name of the item being used
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logUseItem(player: PlayerState, itemName: String)(logger: BattleLogger): BattleLogger

  /**
   * Logs the outcome of a move-related success check.
   *
   * @param move
   *   the move being resolved
   * @param boolean
   *   true if the move succeeds, false otherwise
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logMoveRoll(move: Move, boolean: Boolean)(logger: BattleLogger): BattleLogger

  /**
   * Logs a successful Pokémon switch.
   *
   * @param from
   *   the Pokémon switched out
   * @param to
   *   the Pokémon switched in
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logSwitchPokemon(from: PokemonState, to: PokemonState)(logger: BattleLogger): BattleLogger

  /**
   * Logs that a Pokémon cannot act because it is knocked out.
   *
   * @param pokemonState
   *   the Pokémon that cannot act
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logCannotMoveIsKo(pokemonState: PokemonState)(logger: BattleLogger): BattleLogger

  /**
   * Logs that a Pokémon cannot use a move because it has no remaining PP.
   *
   * @param pokemonState
   *   the Pokémon attempting to use the move
   * @param move
   *   the move that cannot be used
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logNotEnoughPP(pokemonState: PokemonState, move: Move)(logger: BattleLogger): BattleLogger

  /**
   * Logs that a Pokémon cannot move.
   *
   * @param pokemonState
   *   the Pokémon that cannot act
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logCannotMove(pokemonState: PokemonState)(logger: BattleLogger): BattleLogger

  /**
   * Logs that a Pokémon hits itself.
   *
   * @param pokemonState
   *   the Pokémon that hits itself
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logSelfHit(pokemonState: PokemonState)(logger: BattleLogger): BattleLogger

  /**
   * Logs that a status condition has been inflicted on a Pokémon.
   *
   * @param pokemonState
   *   the affected Pokémon
   * @param status
   *   the inflicted status condition
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logStatusInflicted(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger

  /**
   * Logs damage dealt to a Pokémon by a status condition.
   *
   * @param pokemonState
   *   the affected Pokémon
   * @param status
   *   the status condition causing the damage
   * @param damage
   *   the amount of HP lost
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logStatusDamage(pokemonState: PokemonState, status: AlteredStatus, damage: Int)(logger: BattleLogger): BattleLogger

  /**
   * Logs that a status condition prevents a Pokémon from moving.
   *
   * @param pokemonState
   *   the affected Pokémon
   * @param status
   *   the status condition preventing movement
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logStatusPreventsMove(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger

  /**
   * Logs that a status condition is still active on a Pokémon.
   *
   * @param pokemonState
   *   the affected Pokémon
   * @param status
   *   the status condition that remains active
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logStatusContinues(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger

  /**
   * Logs that a status condition has ended for a Pokémon.
   *
   * @param pokemonState
   *   the affected Pokémon
   * @param status
   *   the status condition that has ended
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logStatusEnded(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger

  /**
   * Logs residual damage dealt by weather at the end of the turn.
   *
   * @param pokemonState
   * the Pokémon that takes weather damage
   * @param weather
   * the weather condition causing the damage
   * @param damage
   * the amount of HP lost
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherDamage(pokemonState: PokemonState, weather: Weather, damage: Int)(logger: BattleLogger): BattleLogger

  /**
   * Logs HP restored by weather at the end of the turn.
   *
   * @param pokemonState
   * the Pokémon that is healed by weather
   * @param weather
   * the weather condition causing the healing
   * @param heal
   * the amount of HP restored
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherHeal(pokemonState: PokemonState, weather: Weather, heal: Int)(logger: BattleLogger): BattleLogger

  /**
   * Logs that weather modifies the power of moves of a given type.
   *
   * @param weather
   * the active weather condition
   * @param moveType
   * the affected move type
   * @param multiplier
   * the power multiplier applied by weather
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherPowerModifier(weather: Weather, moveType: Type, multiplier: Double)(logger: BattleLogger): BattleLogger

  /**
   * Logs that weather changes the global move accuracy.
   *
   * @param weather
   * the active weather condition
   * @param multiplier
   * the accuracy multiplier applied by weather
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherAccuracyModifier(weather: Weather, multiplier: Double)(logger: BattleLogger): BattleLogger

  /**
   * Logs that weather causes moves of a given type to ignore accuracy checks.
   *
   * @param weather
   * the active weather condition
   * @param moveType
   * the move type affected by the weather rule
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherAccuracyIgnored(weather: Weather, moveType: Type)(logger: BattleLogger): BattleLogger

  /**
   * Logs that weather prevents freeze from being inflicted.
   *
   * @param weather
   * the active weather condition preventing freeze
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherFreezeBlocked(weather: Weather)(logger: BattleLogger): BattleLogger

  /**
   * Logs that weather modifies the probability of inflicting sleep for a move type.
   *
   * @param weather
   * the active weather condition
   * @param moveType
   * the affected move type
   * @param multiplier
   * the sleep chance multiplier applied by weather
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherSleepModifier(weather: Weather, moveType: Type, multiplier: Double)(logger: BattleLogger): BattleLogger

  /**
   * Logs that weather overrides the paralysis chance for a move type.
   *
   * @param weather
   * the active weather condition
   * @param moveType
   * the affected move type
   * @param chance
   * the paralysis chance enforced by weather
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherParalysisOverride(weather: Weather, moveType: Type, chance: Double)(logger: BattleLogger): BattleLogger

  extension (logger: BattleLogger)
    /**
     * Renders the complete battle log as a single string.
     *
     * @return
     *   the textual representation of the current log
     */
    def getLog: String

    /**
     * Adds a player label to the battle log.
     *
     * @param playerName
     *   the name of the player to include in the log
     * @return
     *   the updated logger
     */
    def setPlayer(playerName: String): BattleLogger

object BattleLogger:
  /**
   * Immutable battle log representation.
   *
   * The log is internally stored as a list of textual entries.
   */
  opaque type BattleLogger = List[String]

  /**
   * Creates an empty battle log.
   *
   * @return
   *   an empty logger
   */
  def emptyLogger: BattleLogger = List.empty

  extension (logger: BattleLogger)
    /**
     * Renders the complete battle log as a single string.
     *
     * @return
     *   the textual representation of the current log
     */
    def getLog: String = logger.foldLeft("")((acc, l) => l.concat(acc))

    /**
     * Adds a player label to the battle log.
     *
     * @param playerName
     *   the name of the player to include in the log
     * @return
     *   the updated logger
     */
    def setPlayer(playerName: String): BattleLogger = s"\n[$playerName]: " :: logger

  /**
   * Adds a generic message to the battle log.
   *
   * @param message
   *   the message to record
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logMessage(message: String)(logger: BattleLogger): BattleLogger = s"$message" :: logger

  /**
   * Adds an error message to the battle log.
   *
   * @param message
   *   the error message to record
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logError(message: String)(logger: BattleLogger): BattleLogger = s"ERROR: $message" :: logger

  /**
   * Logs the usage of an item by a player.
   *
   * @param player
   *   the player using the item
   * @param item
   *   the item being used
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logUseItem(player: PlayerState, item: Item)(logger: BattleLogger): BattleLogger =
    s"${player.getActive.species.name} used the item ${item.name} - ${item.description}" :: logger

  /**
   * Logs that a Pokémon cannot act because it is knocked out.
   *
   * @param pokemonState
   *   the Pokémon that cannot act
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logCannotMoveIsKo(pokemonState: PokemonState)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} is KO and cannot attack" :: logger

  /**
   * Logs a successful Pokémon switch.
   *
   * @param from
   *   the Pokémon switched out
   * @param to
   *   the Pokémon switched in
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logSwitchPokemon(from: PokemonState, to: PokemonState)(logger: BattleLogger): BattleLogger =
    s"Switch between ${from.species.name} and ${to.species.name} happened successfully" :: logger

  /**
   * Logs the outcome of a move-related success check.
   *
   * @param move
   *   the move being resolved
   * @param boolean
   *   true if the move succeeds, false otherwise
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logMoveRoll(move: Move, boolean: Boolean)(logger: BattleLogger): BattleLogger =
    s"The move ${move.name} launch was ${if boolean then "successful" else "unsuccessful"}" :: logger

  /**
   * Logs that a Pokémon cannot use a move because it has no remaining PP.
   *
   * @param pokemonState
   *   the Pokémon attempting to use the move
   * @param move
   *   the move that cannot be used
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logNotEnoughPP(pokemonState: PokemonState, move: Move)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} has not enough PP to use ${move.name}" :: logger

  /**
   * Logs that a Pokémon cannot move.
   *
   * @param pokemonState
   *   the Pokémon that cannot act
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logCannotMove(pokemonState: PokemonState)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} cannot move" :: logger

  /**
   * Logs that a Pokémon hits itself.
   *
   * @param pokemonState
   *   the Pokémon that hits itself
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logSelfHit(pokemonState: PokemonState)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} hit itself" :: logger

  /**
   * Logs that a status condition has been inflicted on a Pokémon.
   *
   * @param pokemonState
   *   the affected Pokémon
   * @param status
   *   the inflicted status condition
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logStatusInflicted(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} got status [${status.toString}]" :: logger

  /**
   * Logs damage dealt to a Pokémon by a status condition.
   *
   * @param pokemonState
   *   the affected Pokémon
   * @param status
   *   the status condition causing the damage
   * @param damage
   *   the amount of HP lost
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logStatusDamage(pokemonState: PokemonState, status: AlteredStatus, damage: Int)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} is dealt $damage HP damage caused by status [${status.toString}]" :: logger

  /**
   * Logs that a status condition prevents a Pokémon from moving.
   *
   * @param pokemonState
   *   the affected Pokémon
   * @param status
   *   the status condition preventing movement
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logStatusPreventsMove(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} is unable to move because of status [${status.toString}]" :: logger

  /**
   * Logs that a status condition is still active on a Pokémon.
   *
   * @param pokemonState
   *   the affected Pokémon
   * @param status
   *   the status condition that remains active
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logStatusContinues(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} is still under the effect of status [${status.toString}]" :: logger

  /**
   * Logs that a status condition has ended for a Pokémon.
   *
   * @param pokemonState
   *   the affected Pokémon
   * @param status
   *   the status condition that has ended
   * @param logger
   *   the logger to update
   * @return
   *   the updated logger
   */
  def logStatusEnded(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} is no more under the effect of status [${status.toString}]" :: logger

  /**
   * Logs residual HP damage caused by weather at the end of the turn.
   *
   * @param pokemonState
   * the Pokémon that takes the weather damage
   * @param weather
   * the weather condition causing the damage
   * @param damage
   * the amount of HP lost
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherDamage(pokemonState: PokemonState, weather: scalamon.domain.weather.Weather, damage: Int)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} takes $damage HP damage due to weather [$weather]" :: logger

  /**
   * Logs HP restored by weather at the end of the turn.
   *
   * @param pokemonState
   * the Pokémon that is healed by the weather
   * @param weather
   * the weather condition causing the healing
   * @param heal
   * the amount of HP restored
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherHeal(pokemonState: PokemonState, weather: scalamon.domain.weather.Weather, heal: Int)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} restores $heal HP due to weather [$weather]" :: logger

  /**
   * Logs that weather modifies the power of moves of a given type.
   *
   * @param weather
   * the active weather condition
   * @param moveType
   * the move type whose power is being modified
   * @param multiplier
   * the power multiplier applied by weather
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherPowerModifier(weather: scalamon.domain.weather.Weather, moveType: scalamon.domain.types.Type, multiplier: Double)(logger: BattleLogger): BattleLogger =
    s"Weather [$weather] modifies power of [$moveType] moves by x$multiplier" :: logger

  /**
   * Logs that weather changes the global move accuracy.
   *
   * @param weather
   * the active weather condition
   * @param multiplier
   * the accuracy multiplier applied by weather
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherAccuracyModifier(weather: scalamon.domain.weather.Weather, multiplier: Double)(logger: BattleLogger): BattleLogger =
    s"Weather [$weather] changes global accuracy by x$multiplier" :: logger

  /**
   * Logs that weather causes moves of a given type to ignore accuracy checks.
   *
   * @param weather
   * the active weather condition
   * @param moveType
   * the move type that ignores accuracy because of weather
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherAccuracyIgnored(weather: scalamon.domain.weather.Weather, moveType: scalamon.domain.types.Type)(logger: BattleLogger): BattleLogger =
    s"Weather [$weather] causes [$moveType] moves to ignore accuracy checks" :: logger

  /**
   * Logs that weather prevents freeze from being inflicted.
   *
   * @param weather
   * the active weather condition preventing freeze
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherFreezeBlocked(weather: scalamon.domain.weather.Weather)(logger: BattleLogger): BattleLogger =
    s"Weather [$weather] prevents freeze from being applied" :: logger

  /**
   * Logs that weather modifies the sleep chance of moves of a given type.
   *
   * @param weather
   * the active weather condition
   * @param moveType
   * the move type whose sleep chance is being modified
   * @param multiplier
   * the sleep chance multiplier applied by weather
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherSleepModifier(weather: scalamon.domain.weather.Weather, moveType: scalamon.domain.types.Type, multiplier: Double)(logger: BattleLogger): BattleLogger =
    s"Weather [$weather] modifies sleep chance for [$moveType] moves by x$multiplier" :: logger

  /**
   * Logs that weather overrides the paralysis chance of moves of a given type.
   *
   * @param weather
   * the active weather condition
   * @param moveType
   * the move type whose paralysis chance is being overridden
   * @param chance
   * the paralysis chance enforced by weather
   * @param logger
   * the logger to update
   * @return
   * the updated logger
   */
  def logWeatherParalysisOverride(weather: scalamon.domain.weather.Weather, moveType: scalamon.domain.types.Type, chance: Double)(logger: BattleLogger): BattleLogger =
    s"Weather [$weather] overrides paralysis chance for [$moveType] moves to $chance" :: logger