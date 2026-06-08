package scalamon.logics.state

trait PlayerStateModule extends StateComponent:
  type PlayerState
  type PokemonState
  type PokemonId
  override type SubComponent = PokemonState

  def playerState(team: Map[PokemonId, PokemonState], active: PokemonId): PlayerState

  extension (ps: PlayerState)
    def active(f: Modifier): PlayerState
    def bench(f: Modifier): PlayerState
    def all(f: Modifier): PlayerState
    def allThat(p: PokemonState => Boolean)(f: Modifier): PlayerState
    def switchActive(newActive: String): PlayerState


object PlayerStateModuleImpl extends PlayerStateModule:
  case class Ps(team: Map[String, PokemonState], activeId: String)
  override type PlayerState = Ps
  override type PokemonState = PokemonStateModuleImpl.PokemonState
  override type PokemonId = String
  def playerState(team: Map[String, PokemonState], active: String): PlayerState = Ps(team, active)

  private def mapValues[K, V](m: Map[K, V])(f: V => V): Map[K, V] = m.map(e => (e._1, f(e._2)))

  extension (ps: PlayerState)
    infix def active(f: Modifier): PlayerState =
      ps.copy(team = ps.team.updated(ps.activeId, f(ps.team(ps.activeId))))

    infix def bench(f: Modifier): PlayerState =
      ps.copy(team = ps.team.map(e => if e._1 == ps.activeId then (e._1, e._2) else (e._1, f(e._2))))

    infix def all(f: Modifier): PlayerState = ps.copy(team = mapValues(ps.team)(f))

    infix def allThat(p: PokemonState => Boolean)(f: Modifier): PlayerState =
      ps.copy(team = mapValues(ps.team)(s => if p(s) then f(s) else s))

    infix def switchActive(newActive: String): PlayerState = Ps(ps._1, newActive)