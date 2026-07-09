package scalamon.logics.log

import scalamon.domain.actions.Items.Item
import scalamon.domain.moves.Move
import scalamon.logics.state.BattleStateImpl.PlayerState
import scalamon.logics.state.PokemonStateModuleImpl.{PokemonState, currentHp}

trait BattleLogger:
  type BattleLogger
  
  def logError(message: String)(battleLogger: BattleLogger): BattleLogger
  def logMessage(message: String)(battleLogger: BattleLogger): BattleLogger

  def logUseItem(player: PlayerState, itemName: String)(logger: BattleLogger): BattleLogger
  def logMoveRoll(move: Move, boolean: Boolean)(logger: BattleLogger): BattleLogger
  def logSwitchPokemon(from: PokemonState, to: PokemonState)(logger: BattleLogger): BattleLogger


object BattleLogger:
  override opaque type BattleLogger = List[String]

  def emptyLogger: BattleLogger = List.empty

  extension (logger: BattleLogger)
    def logUseMove(attacker: PokemonState, defender: PokemonState, move: Option[Move]): BattleLogger = move match
      case Some(m) => s"${attacker.species.name} usa ${m.name} | ${defender.species.name} HP: ${defender.currentHp}" :: logger
      case _ => "Nessuna mossa trovata" :: logger

    def logIsKo(pokemonState: PokemonState): BattleLogger =
      s"${pokemonState.species.name} e' KO e non puo' attaccare" :: logger

    def logNotEnoughPP(pokemonState: PokemonState, move: Move): BattleLogger =
      s"${pokemonState.species.name} non ha abbastanza PP per usare ${move.name}" :: logger

    def getLog: String = logger.foldLeft("")((acc, l) => l.concat(acc + "\n"))

  def logUseItem(player: PlayerState, item: String)(logger: BattleLogger): BattleLogger = item match
    case Some(item) => s"${player.getActive.species.name} ha usato l'oggetto ${item}" :: logger
    case _ => "Nessun oggetto trovato" :: logger

  def logSwitchPokemon(from: PokemonState, to: PokemonState)(logger: BattleLogger): BattleLogger =
    s"Scambio tra ${from.species.name} e ${to.species.name} avvenuto" :: logger

  def logMoveRoll(move: Move, boolean: Boolean)(logger: BattleLogger): BattleLogger =
    s"Il lancio della mossa ${move.name} e' ${if boolean then "andato a buon fine" else "fallito"}" :: logger
  
  def logError(message: String)(logger: BattleLogger): BattleLogger =
    s"ERROR: $message" :: logger