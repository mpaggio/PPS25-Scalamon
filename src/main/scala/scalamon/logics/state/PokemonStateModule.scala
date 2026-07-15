package scalamon.logics.state

trait PokemonStateModule extends StateComponent:
  type PokemonState
  type PokemonSpecies
  type AlteredStatus
  type StatsState
  type HP
  type MoveState
  override type SubComponent = StatsState

  def pokemonInitialState(species: PokemonSpecies, moves: Map[String, MoveState]): PokemonState

  extension (ps: PokemonState)
    def currentHp(f: HP => HP): PokemonState
    def modifyStats(f: Modifier): PokemonState
    def addStatus(status: AlteredStatus): PokemonState
    def updateMove(moveName: String)(f: MoveState => MoveState): PokemonState
    def moveState(moveName: String): MoveState
    def takeDamage(amount: Int): PokemonState
    def heal(amount: Int): PokemonState


object PokemonStateModuleImpl extends PokemonStateModule:
  import StatsStateModuleImpl.*
  case class Ps(
    currentHp: HP,
    modifiedStats: StatsState,
    moves: Map[String, MoveState],
    status: List[AlteredStatus] = List(),
    species: PokemonSpecies)
  override type PokemonState = Ps
  override type PokemonSpecies = scalamon.domain.pokemon.Pokemon
  override type AlteredStatus = StateTransformerModuleImpl.TransformerFlatMapper
  override type StatsState = StatsStateModuleImpl.StatsState
  override type HP = StatsStateModuleImpl.Stat
  override type MoveState = scalamon.logics.state.MoveStateModuleImpl.MoveState

  def pokemonInitialState(species: PokemonSpecies, moves: Map[String, MoveState]): PokemonState =
    Ps(species.baseStats.hp.toInt, statsInitialState(species.baseStats), moves, List(), species)

  extension (ps: PokemonState)
    infix def currentHp(f: HP => HP): PokemonState = ps.copy(currentHp = f(ps.currentHp))
    infix def modifyStats(f: Modifier): PokemonState = ps.copy(modifiedStats = f(ps.modifiedStats))
    infix def addStatus(status: AlteredStatus): PokemonState = ps.copy(status = status :: ps.status)
    infix def updateMove(moveName: String)(f: MoveState => MoveState): PokemonState =
      ps.copy(moves = ps.moves.updated(moveName, f(ps.moves(moveName))))
    def moveState(moveName: String): MoveState = ps.moves(moveName)
    infix def takeDamage(amount: Int): PokemonState = ps.currentHp(_ decrease amount)
    infix def heal(amount: Int): PokemonState = ps.currentHp(_ increase amount)

