package scalamon.logics.state

import scalamon.domain.pokemon
import scalamon.domain.pokemon.statistics

trait PokemonStateModule:
  type PokemonState
  type Stats
  //type AlteratedStatus

  def pokemonState(hp: Int, stats: Stats): PokemonState

  extension (ps: PokemonState)
    def hp: Int
    //def status: List[AlteratedStatus]
    //def addStatus(status: AlteratedStatus): PokemonState
    def damage(amount: Int): PokemonState
    def heal(amount: Int): PokemonState


object PokemonStateModuleImpl extends PokemonStateModule:
  case class Ps(hp: Int, stats: statistics.Stats)
  override type PokemonState = Ps
  override type Stats = statistics.Stats

  def pokemonState(hp: Int, stats: Stats): PokemonState = Ps(hp, stats)

  extension (ps: PokemonState)
    infix def hp: Int = ps.hp
    infix def damage(amount: Int): PokemonState = ps.copy(hp = ps.hp - amount)
    infix def heal(amount: Int): PokemonState = ps.copy(hp = ps.hp + amount)
    infix def stats(f: Stats => Stats): PokemonState = ps.copy(stats = f(ps.stats))