package scalamon.logics.state

trait PokemonStateModule extends StateComponent:
  type PokemonState
  type PokemonSpecies
  type AlteredStatus
  type StatsState
  type HP
  override type SubComponent = StatsState

  def pokemonInitialState(species: PokemonSpecies): PokemonState

  extension (ps: PokemonState)
    def currentHp(f: HP => HP): PokemonState
    def modifyStats(f: Modifier): PokemonState
    def addStatus(status: AlteredStatus): PokemonState


object PokemonStateModuleImpl extends PokemonStateModule:
  case class Ps(currentHp: HP, modifiedStats: StatsState, status: List[AlteredStatus] = List(), species: PokemonSpecies)
  override type PokemonState = Ps
  override type PokemonSpecies = scalamon.domain.pokemon.Pokemon
  override type AlteredStatus = StateTransformerModuleImpl.TransformerFlatMapper
  override type StatsState = StatsStateModuleImpl.StatsState
  override type HP = StatsStateModuleImpl.Stat

  def pokemonInitialState(species: PokemonSpecies): PokemonState =
    Ps(species.baseStats.hp, species.baseStats, List(), species)

  extension (ps: PokemonState)
    infix def currentHp(f: HP => HP): PokemonState = ps.copy(currentHp = f(ps.currentHp))
    infix def modifyStats(f: Modifier): PokemonState = ps.copy(modifiedStats = f(ps.modifiedStats))
    infix def addStatus(status: AlteredStatus): PokemonState = ps.copy(status = status :: ps.status)

