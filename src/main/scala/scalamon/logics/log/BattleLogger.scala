package scalamon.logics.log

import scalamon.domain.actions.Items.Item
import scalamon.domain.alteredStatus.AlteredStatus
import scalamon.domain.moves.Move
import scalamon.logics.state.BattleStateImpl.PlayerState
import scalamon.logics.state.PokemonStateModuleImpl.PokemonState

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