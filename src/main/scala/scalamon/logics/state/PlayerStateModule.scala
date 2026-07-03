package scalamon.logics.state

trait PlayerStateModule extends StateComponent:
  type PlayerState
  protected type PokemonState
  type PokemonId
  override protected type State = PlayerState
  override protected type InnerState = PokemonState

  def playerState(team: Map[PokemonId, PokemonState], active: PokemonId): PlayerState

  def switchActive(newActive: PokemonId): Op
  def active(f: InnerOp): Op
  def bench(f: InnerOp): Op
  def all(f: InnerOp): Op
  def allThat(p: PokemonState => Boolean)(f: InnerOp): Op

object PlayerStateModuleImpl extends PlayerStateModule:
  case class Ps(team: Map[String, PokemonState], activeId: String):
    def getActive: PokemonState = team(activeId)

  override type PlayerState = Ps
  override type PokemonState = PokemonStateModuleImpl.PokemonState
  override type PokemonId = String

  def playerState(team: Map[PokemonId, PokemonState], active: PokemonId): PlayerState = Ps(team, active)

  private def mapValues[K, V](m: Map[K, V])(f: V => V): Map[K, V] = m.map(e => (e._1, f(e._2)))

  def switchActive(newActive: PokemonId): Op = ps => Ps(ps._1, newActive)
  def active(f: InnerOp): Op = ps => ps.copy(team = ps.team.updated(ps.activeId, f(ps.getActive)))
  def bench(f: InnerOp): Op = ps => ps.copy(team = ps.team.map(e => if e._1 == ps.activeId then (e._1, e._2) else (e._1, f(e._2))))
  def all(f: InnerOp): Op = ps => ps.copy(team = mapValues(ps.team)(f))
  def allThat(p: PokemonState => Boolean)(f: InnerOp): Op = ps => ps.copy(team = mapValues(ps.team)(s => if p(s) then f(s) else s))
