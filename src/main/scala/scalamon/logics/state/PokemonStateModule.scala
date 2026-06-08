package scalamon.logics.state

trait PokemonStateModule extends StateComponent:
  type PokemonState
  type PokemonSpecies
  type AlteratedStatus
  type StatsState
  type HP
  override type SubComponent = StatsState

  def pokemonInitialState(species: PokemonSpecies): PokemonState

  extension (ps: PokemonState)
    def status: List[AlteratedStatus]
    def addStatus(status: AlteratedStatus): PokemonState
    def damage(amount: HP): PokemonState
    def heal(amount: HP): PokemonState
    def modifyStats(f: Modifier): PokemonState


object PokemonStateModuleImpl extends PokemonStateModule:
  case class Ps(currentHp: HP, modifiedStats: StatsState, status: List[AlteratedStatus] = List(), species: PokemonSpecies)
  override type PokemonState = Ps
  override type PokemonSpecies = scalamon.domain.pokemon.Pokemon
  override type AlteratedStatus = AlteratedStatusModuleImpl.AlteratedStatus
  override type StatsState = StatsStateModuleImpl.StatsState
  override type HP = Int

  def pokemonInitialState(species: PokemonSpecies): PokemonState =
    Ps(species.baseStats.hp.toInt, species.baseStats, List(), species)

  extension (ps: PokemonState)
    infix def damage(amount: HP): PokemonState = ps.copy(currentHp = ps.currentHp - amount)
    infix def heal(amount: HP): PokemonState = ps.copy(currentHp = ps.currentHp + amount)
    infix def modifyStats(f: Modifier): PokemonState = ps.copy(modifiedStats = f(ps.modifiedStats))
    infix def status: List[AlteratedStatus] = ps.status
    infix def addStatus(status: AlteratedStatus): PokemonState = ps.copy(status = status :: ps.status)

