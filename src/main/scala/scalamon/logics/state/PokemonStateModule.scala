package scalamon.logics.state

import scalamon.domain.pokemon

trait PokemonStateModule:
  type PokemonState
  type Stats
  type AlteratedStatus

  def pokemonState(hp: Int, stats: Stats): PokemonState

  extension (ps: PokemonState)
    def hp: Int
    def status: List[AlteratedStatus]
    def addStatus(status: AlteratedStatus): PokemonState
    def damage(amount: Int): PokemonState
    def heal(amount: Int): PokemonState


object PokemonStateModuleImpl extends PokemonStateModule:
  case class Ps(hp: Int, stats: Stats, status: List[AlteratedStatus] = List())
  override type PokemonState = Ps
  override type Stats = pokemon.Stats
  override type AlteratedStatus = AlteratedStatusModuleImpl.AlteratedStatus

  def pokemonState(hp: Int, stats: Stats): PokemonState = Ps(hp, stats)

  extension (ps: PokemonState)
    infix def hp: Int = ps.hp
    infix def damage(amount: Int): PokemonState = ps.copy(hp = ps.hp - amount)
    infix def heal(amount: Int): PokemonState = ps.copy(hp = ps.hp + amount)
    infix def stats(f: Stats => Stats): PokemonState = ps.copy(stats = f(ps.stats))
    infix def status: List[AlteratedStatus] = ps.status
    infix def addStatus(status: AlteratedStatus): PokemonState = ps.copy(status = status :: ps.status)

