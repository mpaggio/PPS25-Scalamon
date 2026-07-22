package scalamon.logics.state

/**
 * The `PokemonStateModule` trait defines the structure and operations for managing the state of a Pokémon in a battle.
 * It extends the `StateComponent` trait, providing a framework for manipulating Pokémon states, including their stats,
 * moves, and status conditions.
 */
trait PokemonStateModule extends StateComponent:
  type PokemonState
  protected type StatsState
  protected type MoveState
  override protected type State = PokemonState
  override protected type InnerState = StatsState
  type PokemonSpecies
  type AlteredStatus
  type HP

  /**
   * Creates the initial state of a Pokémon given its species and move states.
   */
  def pokemonInitialState(species: PokemonSpecies, moves: Map[String, MoveState]): PokemonState

  /**
   * Modifies the current HP of the Pokémon.
   * The function `f` takes the current HP and returns the new HP.
   * The new HP is clamped between 0 and the maximum HP of the Pokémon.
   */
  def currentHp(f: HP => HP): Op

  /**
   * Modifies the stats of the Pokémon.
   * The function `f` takes the current stats and returns the new stats.
   */
  def modifyStats(f: InnerOp): Op

  /**
   * Adds a status condition to the Pokémon.
   * If the Pokémon already has a status condition of the same type, it will be replaced.
   */
  def addStatus(status: AlteredStatus): Op

  /**
   * Removes a status condition from the Pokémon.
   * If the Pokémon does not have the specified status condition, nothing happens.
   */
  def removeStatus(statusType: AlteredStatus): Op

  /**
   * Modifies the move states of the Pokémon.
   * The function `f` takes the current move state and returns the new move state.
   */
  def moves(f: MoveState => MoveState): Op

  /**
   * Updates the state of a specific move of the Pokémon.
   * The function `f` takes the current move state and returns the new move state.
   */
  def updateMove(moveName: String)(f: MoveState => MoveState): Op

  /**
   * Applies damage to the Pokémon, reducing its current HP by the specified amount.
   * The new HP is clamped between 0 and the maximum HP of the Pokémon.
   */
  def takeDamage(amount: Int): Op

  /**
   * Heals the Pokémon, increasing its current HP by the specified amount.
   * The new HP is clamped between 0 and the maximum HP of the Pokémon.
   */
  def heal(amount: Int): Op

  extension (ps: PokemonState)

    /**
     * Retrieves the state of a specific move of the Pokémon by its name.
     */
    def moveState(moveName: String): MoveState

    /**
     * Clears all status conditions from the Pokémon, returning a new state with no status conditions.
     */
    def clearStatusCondition: PokemonState

    /**
     * Retrieves the current status condition of the Pokémon, if any.
     * Returns `None` if the Pokémon has no status conditions.
     */
    def statusCondition: Option[AlteredStatus]

    /**
     * Retrieves the maximum HP of the Pokémon based on its modified stats.
     */
    def maxHp: Int

/**
 * Concrete implementation of the PokemonStateModule.
 * It defines the internal representation of the Pokémon state and provides operations to manipulate it.
 * In addition, it exposes the parameters of the Pokémon state to be read.
 */
object PokemonStateModuleImpl extends PokemonStateModule:
  import StatsStateModuleImpl.*
  case class Pks(
                  currentHp: HP,
                  modifiedStats: StatsState,
                  moves: Map[String, MoveState],
                  status: Set[AlteredStatus] = Set(),
                  species: PokemonSpecies)
  override type PokemonState = Pks
  override type PokemonSpecies = scalamon.domain.pokemon.Pokemon
  override type AlteredStatus = scalamon.domain.alteredStatus.AlteredStatus
  override type StatsState = StatsStateModuleImpl.StatsState
  override type HP = StatsStateModuleImpl.Stat
  override type MoveState = scalamon.logics.state.MoveStateModuleImpl.MoveState

  def pokemonInitialState(species: PokemonSpecies, moves: Map[String, MoveState]): PokemonState =
    Pks(species.baseStats.hp.toInt, statsInitialState(species.baseStats), moves, Set(), species)

  def currentHp(f: Stat => Stat): Op = ps => ps.copy(currentHp = f(ps.currentHp).clamped(0, ps.maxHp))
  def modifyStats(f: InnerOp): Op = ps => ps.copy(modifiedStats = f(ps.modifiedStats))
  def addStatus(status: AlteredStatus): Op = ps =>
    val clearedState = removeStatus(status)(ps)
    clearedState.copy(status = clearedState.status + status)
  def removeStatus(status: AlteredStatus): Op = ps => ps.copy(status = ps.status.filterNot(_.ordinal == status.ordinal))
  def moves(f: MoveState => MoveState): Op = ps => ps.copy(moves = ps.moves.map(e => (e._1, f(e._2))))
  def updateMove(moveName: String)(f: MoveState => MoveState): Op = ps =>
    ps.copy(moves = ps.moves.updated(moveName, f(ps.moves(moveName))))

  def takeDamage(amount: Int): Op = currentHp(decrease(amount))
  def heal(amount: Int): Op = currentHp(increase(amount))

  extension (pks: PokemonState)
    def moveState(moveName: String): MoveState = pks.moves(moveName)
    def maxHp: Int = pks.modifiedStats.hp
    def statusCondition: Option[AlteredStatus] = pks.status.headOption
    def clearStatusCondition: PokemonState = pks.copy(status = Set.empty)