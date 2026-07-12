package scalamon.logics.log

import scalamon.domain.actions.Items.Item
import scalamon.domain.moves.{AlteredStatus, Move}
import scalamon.logics.state.BattleStateImpl.PlayerState
import scalamon.logics.state.PokemonStateModuleImpl.PokemonState

trait BattleLogger:
  type BattleLogger
  
  def logError(message: String)(battleLogger: BattleLogger): BattleLogger
  def logMessage(message: String)(battleLogger: BattleLogger): BattleLogger

  def logUseItem(player: PlayerState, itemName: String)(logger: BattleLogger): BattleLogger
  def logMoveRoll(move: Move, boolean: Boolean)(logger: BattleLogger): BattleLogger
  def logSwitchPokemon(from: PokemonState, to: PokemonState)(logger: BattleLogger): BattleLogger
  def logCannotMoveIsKo(pokemonState: PokemonState)(logger: BattleLogger): BattleLogger
  def logNotEnoughPP(pokemonState: PokemonState, move: Move)(logger: BattleLogger): BattleLogger
  def logCannotMove(pokemonState: PokemonState)(logger: BattleLogger): BattleLogger
  def logSelfHit(pokemonState: PokemonState)(logger: BattleLogger): BattleLogger

  def logStatusInflicted(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger
  def logStatusDamage(pokemonState: PokemonState, status: AlteredStatus, damage: Int)(logger: BattleLogger): BattleLogger
  def logStatusPreventsMove(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger
  def logStatusContinues(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger
  def logStatusEnded(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger

  extension (logger: BattleLogger)
    def getLog: String
    def setPlayer(playerName: String): BattleLogger

object BattleLogger:
  opaque type BattleLogger = List[String]

  def emptyLogger: BattleLogger = List.empty

  extension (logger: BattleLogger)
    def getLog: String = logger.foldLeft("")((acc, l) => l.concat(acc))
    def setPlayer(playerName: String): BattleLogger = s"\n[$playerName]: " :: logger

  def logMessage(message: String)(logger: BattleLogger): BattleLogger = s"$message" :: logger
  def logError(message: String)(logger: BattleLogger): BattleLogger = s"ERROR: $message" :: logger

  def logUseItem(player: PlayerState, item: Item)(logger: BattleLogger): BattleLogger =
    s"${player.getActive.species.name} ha usato l'oggetto ${item.name} - ${item.shortDescription}" :: logger

  def logCannotMoveIsKo(pokemonState: PokemonState)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} e' KO e non puo' attaccare" :: logger

  def logSwitchPokemon(from: PokemonState, to: PokemonState)(logger: BattleLogger): BattleLogger =
    s"Scambio tra ${from.species.name} e ${to.species.name} avvenuto" :: logger

  def logMoveRoll(move: Move, boolean: Boolean)(logger: BattleLogger): BattleLogger =
    s"Il lancio della mossa ${move.name} e' ${if boolean then "andato a buon fine" else "fallito"}" :: logger

  def logNotEnoughPP(pokemonState: PokemonState, move: Move)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} non ha abbastanza PP per usare ${move.name}" :: logger

  def logCannotMove(pokemonState: PokemonState)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} non puo' muoversi" :: logger

  def logSelfHit(pokemonState: PokemonState)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} si e' colpito da solo" :: logger

  def logStatusInflicted(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} got status [${status.toString}]" :: logger

  def logStatusDamage(pokemonState: PokemonState, status: AlteredStatus, damage: Int)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} is dealt $damage HP damage caused by status [${status.toString}]" :: logger

  def logStatusPreventsMove(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} is unable to move because of status [${status.toString}]" :: logger

  def logStatusContinues(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} is still under the effect of status [${status.toString}]" :: logger

  def logStatusEnded(pokemonState: PokemonState, status: AlteredStatus)(logger: BattleLogger): BattleLogger =
    s"${pokemonState.species.name} is no more under the effect of status [${status.toString}]" :: logger
