package scalamon.logics.state

/**
 * The PlayerStateModule defines the structure and operations for managing the state of a player in a Pokémon battle.
 * It provides methods to manipulate the player's team, active Pokémon, and items.
 */
trait PlayerStateModule extends StateComponent:
  type PlayerState
  protected type PokemonState
  type PokemonId
  type Items
  override protected type State = PlayerState
  override protected type InnerState = PokemonState

  /**
   * Creates the initial state of a player with the given name, team, active Pokémon, and items.
   */
  def playerInitialState(name: String, team: Map[PokemonId, PokemonState], active: PokemonId, items: Items): PlayerState

  /**
   * Switches the active Pokémon to the one with the given ID.
   */
  def switchActive(newActive: PokemonId): Op

  /**
   * Applies the given operation `f` to the active Pokémon.
   */
  def active(f: InnerOp): Op

  /**
   * Applies the given operation `f` to all Pokémon on the bench (team not active).
   */
  def bench(f: InnerOp): Op

  /**
   * Applies the given operation `f` to all Pokémon in the team.
   */
  def all(f: InnerOp): Op

  /**
   * Applies the given operation `f` to all Pokémon that satisfy the given predicate `p`.
   */
  def allThat(p: PokemonState => Boolean)(f: InnerOp): Op

  /**
   * Applies the given function `f` to the player's items, allowing for modification of the items set.
   */
  def items(f: Items => Items): Op


/**
 * Concrete implementation of the PlayerStateModule.
 * It defines the internal representation of the player state and provides operations to manipulate it.
 * In addition, it exposes the parameters of the player state to be read.
 */
object PlayerStateModuleImpl extends PlayerStateModule:
  override type PlayerState = Ps
  override type PokemonState = PokemonStateModuleImpl.PokemonState
  override type PokemonId = String
  override type Items = Set[scalamon.domain.actions.Item]

  case class BattleFlags(
    isSwitchBlocked: Boolean = false,
    weatherSuppressed: Boolean = false,
    flashFireActive: Boolean = false,
    magicGuardActive: Boolean = false,
    lastMove: Option[scalamon.domain.moves.DamageMove] = None
  )

  case class Ps(name: String, team: Map[String, PokemonState], activeId: String, items: Items, flags: BattleFlags):
    def getActive: PokemonState = team(activeId)

  def playerInitialState(name: String, team: Map[PokemonId, PokemonState], active: PokemonId): PlayerState =
    playerInitialState(name, team, active, Set.empty)
    
  override def playerInitialState(name: String, team: Map[PokemonId, PokemonState], active: PokemonId, items: Items): PlayerState =
    Ps(name, team, active, items, BattleFlags())

  def switchActive(newActive: PokemonId): Op = ps => ps.copy(activeId = newActive)
  def active(f: InnerOp): Op = ps => ps.copy(team = ps.team.updated(ps.activeId, f(ps.getActive)))
  def bench(f: InnerOp): Op = ps => allThat(pks => pks != ps.getActive)(f)(ps)
  def all(f: InnerOp): Op = ps => ps.copy(team = ps.team.map(e => (e._1, f(e._2))))
  def allThat(p: PokemonState => Boolean)(f: InnerOp): Op = all(s => if p(s) then f(s) else s)
  def items(f: Items => Items): Op = ps => ps.copy(items = f(ps.items))
  def updateFlags(f: BattleFlags => BattleFlags): Op = ps => ps.copy(flags = f(ps.flags))