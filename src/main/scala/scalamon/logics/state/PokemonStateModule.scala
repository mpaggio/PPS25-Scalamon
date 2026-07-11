package scalamon.logics.state

trait PokemonStateModule extends StateComponent:
  type PokemonState
  protected type StatsState
  protected type MoveState
  override protected type State = PokemonState
  override protected type InnerState = StatsState
  type PokemonSpecies
  type AlteredStatus
  type HP

  def pokemonInitialState(species: PokemonSpecies, moves: Map[String, MoveState]): PokemonState

  def currentHp(f: HP => HP): Op
  def modifyStats(f: InnerOp): Op
  def addStatus(status: AlteredStatus): Op
  def removeStatus(statusType: AlteredStatus): Op
  def moves(f: MoveState => MoveState): Op
  def updateMove(moveName: String)(f: MoveState => MoveState): Op

  def takeDamage(amount: Int): Op
  def heal(amount: Int): Op

  extension (ps: PokemonState)
    def moveState(moveName: String): MoveState
    def clearStatusCondition: PokemonState
    def statusCondition: Option[AlteredStatus]
    def maxHp: Int


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
  override type AlteredStatus = scalamon.domain.moves.AlteredStatus
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
    def maxHp: Int = pks.species.baseStats.hp.toInt
    def statusCondition: Option[AlteredStatus] = pks.status.headOption
    def clearStatusCondition: PokemonState = pks.copy(status = Set.empty)

    def setStatus(status: AlteredStatus): PokemonState =
      val allAbilities = List(
        Some(pks.species.abilitySlot.primary),
        pks.species.abilitySlot.secondary,
        pks.species.abilitySlot.hidden
      ).flatten
      addStatus(status)(pks)






