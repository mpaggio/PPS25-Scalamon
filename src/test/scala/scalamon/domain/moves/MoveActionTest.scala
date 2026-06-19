package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import DamageMoveCategory.*
import MoveDSL.{move, *}
import scalamon.domain.types.Type.*
import scalamon.logics.state.BattleStateImpl.*
import scalamon.logics.state.PlayerStateModuleImpl.*
import scalamon.logics.state.PokemonStateModuleImpl.*
import scalamon.logics.state.MoveStateModuleImpl.*
import scalamon.domain.pokemon.pokedex.MyPokedex.*
import MoveActionModuleImpl.*

class MoveActionTest extends org.scalatest.funsuite.AnyFunSuite:

  val pokemon: PokemonSpecies = allPokemons.last
  val swift: DamagingMove = move named "Swift" withPower 60 withPP 32 withAccuracy 100 withType Normal as Special
  val initialMoves = Map(swift.name -> moveInitialState(swift))
  val pokemonStartingState: PokemonState = pokemonInitialState(pokemon, initialMoves)
  val battleStartingState: BattleState = battleState(
    userPokemon = playerState(Map.from(List(pokemon.name -> pokemonStartingState)), pokemon.name),
    enemyPokemon = playerState(Map.from(List(pokemon.name -> pokemonStartingState)), pokemon.name)
  )

  test("A move action without Power Points (PP) should never change the state"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 1

    val stateWithZeroPp = battleStartingState self (_ active (_.updateMove(swift.name)(_.currentPp(_ => 0))))
    val transformations: Action = MoveAction(swift).execute
    val battleEndingState = transformations.foldLeft(stateWithZeroPp)((state, transformer) => transformer(state))
    battleEndingState shouldBe stateWithZeroPp

  test("A damaging move action should decrease its Power Points (PP) after every single use"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 1

    val action: MoveAction = MoveAction(swift)
    val state1 = action.execute.foldLeft(battleStartingState)((state, transformer) => transformer(state))
    state1.self.team(pokemon.name).moveState(swift.name).currentPp shouldBe 31
    val state2 = action.execute.foldLeft(state1)((state, transformer) => transformer(state))
    state2.self.team(pokemon.name).moveState(swift.name).currentPp shouldBe 30

  test("Damaging move action with 100% accuracy should always decrease enemy HP and consume PP"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 1

    val battleEndingState =
      MoveAction(swift).execute.foldLeft(battleStartingState)((state, transformer) => transformer(state))
    battleEndingState.opponent.team(pokemon.name).currentHp should be < pokemon.baseStats.hp.toInt
    battleEndingState.self.team(pokemon.name).moveState(swift.name).currentPp shouldBe 31

  test("Damaging move action with 0% accuracy should not decrease enemy HP but must consume PP"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 101

    val battleEndingState =
      MoveAction(swift).execute.foldLeft(battleStartingState)((state, transformer) => transformer(state))
    battleEndingState.opponent.team(pokemon.name).currentHp shouldBe pokemon.baseStats.hp.toInt
    battleEndingState.self.team(pokemon.name).moves(swift.name).currentPp shouldBe 31