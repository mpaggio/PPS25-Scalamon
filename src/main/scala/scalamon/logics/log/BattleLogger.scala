package scalamon.logics.log

import scalamon.domain.moves.Move
import scalamon.logics.state.PokemonStateModuleImpl.{PokemonState, currentHp}

object BattleLogger:
  opaque type BattleLogger = List[String]

  def emptyLogger: BattleLogger = List.empty

  extension (logger: BattleLogger)
    def logUseMove(attacker: PokemonState, defender: PokemonState, move: Option[Move]): BattleLogger = move match
      case Some(m) => s"${attacker.species.name} usa ${m.name} | ${defender.species.name} HP: ${defender.currentHp}" :: logger
      case _ => "Nessuna mossa trovata" :: logger

    def logIsKo(pokemonState: PokemonState): BattleLogger =
      s"${pokemonState.species.name} e' KO e non puo' attaccare" :: logger

    def logNotEnoughPP(pokemonState: PokemonState, move: Move): BattleLogger =
      s"${pokemonState.species.name} non ha abbastanza PP per usare ${move.name}" :: logger

    def logSwitchPokemon(from: PokemonState, to: PokemonState): BattleLogger =
      s"Scambio tra ${from.species.name} e ${to.species.name} avvenuto" :: logger

    def getLog: String = logger.foldLeft("")((acc, l) => l.concat(acc + "\n"))

