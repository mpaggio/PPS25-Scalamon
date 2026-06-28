package scalamon.logics.state

import scalamon.domain.moves.AlteredStatus.Sleeping
import scalamon.domain.pokemon.abilities.Ability.Insomnia

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
    def maxHp: Int
    def statusCondition: Option[AlteredStatus]
    def clearStatusCondition: PokemonState
    def setStatus(status: AlteredStatus): PokemonState


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
  override type AlteredStatus = scalamon.domain.moves.AlteredStatus
  override type StatsState = StatsStateModuleImpl.StatsState
  override type HP = StatsStateModuleImpl.Stat
  override type MoveState = scalamon.logics.state.MoveStateModuleImpl.MoveState

  def pokemonInitialState(species: PokemonSpecies, moves: Map[String, MoveState]): PokemonState =
    Ps(species.baseStats.hp.toInt, statsInitialState(species.baseStats), moves, List(), species)

  extension (ps: PokemonState)
    infix def currentHp(f: HP => HP): PokemonState =
      ps.copy(currentHp = f(ps.currentHp).clamped(0, ps.species.baseStats.hp.toInt))
    infix def modifyStats(f: Modifier): PokemonState = ps.copy(modifiedStats = f(ps.modifiedStats))
    infix def addStatus(status: AlteredStatus): PokemonState = ps.copy(status = status :: ps.status)
    infix def updateMove(moveName: String)(f: MoveState => MoveState): PokemonState =
      ps.copy(moves = ps.moves.updated(moveName, f(ps.moves(moveName))))
    def moveState(moveName: String): MoveState = ps.moves(moveName)
    infix def takeDamage(amount: Int): PokemonState = ps.currentHp(_ decrease amount)
    infix def heal(amount: Int): PokemonState = ps.currentHp(_ increase amount)
    def maxHp: Int = ps.species.baseStats.hp.toInt
    def statusCondition: Option[AlteredStatus] = ps.status.headOption
    def clearStatusCondition: PokemonState = ps.copy(status = List.empty)
    infix def setStatus(status: AlteredStatus): PokemonState = {
      val allAbilities = List(
        Some(ps.species.abilitySlot.primary),
        ps.species.abilitySlot.secondary,
        ps.species.abilitySlot.hidden
      ).flatten

      val sleepImmune = allAbilities.contains(Insomnia)

      val blocked = status match
        case _:  Sleeping => sleepImmune
        case _ => false

      if blocked then
        println(s"[Insomnia] ${ps.species.name} is immune to sleep due to its ability!")
        ps
      else if ps.status.isEmpty then ps.copy(status=List(status))
      else ps
    }

