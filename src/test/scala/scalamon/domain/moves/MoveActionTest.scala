package scalamon.domain.moves

import org.scalatest.matchers.should.Matchers.*
import DamageMoveCategory.*
import StatusMoveCategory.*
import MoveDSL.{move, *}
import MoveEffectDSL.*
import MoveEffectDSL.Effect.*
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

  private def createBattleState(move: Move): BattleState =
    val moveState = Map(move.name -> moveInitialState(move))
    val pokemonState = pokemonInitialState(pokemon, moveState)
    battleState(
      userPokemon = playerState(Map.from(List(pokemon.name -> pokemonState)), pokemon.name),
      enemyPokemon = playerState(Map.from(List(pokemon.name -> pokemonState)), pokemon.name)
    )

  test("Status move with healing effect should heal the user and consume PP"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 1

    val recover = move named "Recover" withPP 32 withAccuracy 100 withType Normal withEffect (Effect healing 50) as Status
    val battleInitialState: BattleState = createBattleState(recover)
    val halfHp = pokemon.baseStats.hp.toInt / 2
    val damagedState: BattleState = battleInitialState self (_ active (_ currentHp (_ => halfHp)))
    val action: MoveAction = MoveAction(recover)
    val battleFinalState: BattleState = action.execute.foldLeft(damagedState)((state, transformer) => transformer(state))
    battleFinalState.self.team(pokemon.name).currentHp shouldBe pokemon.baseStats.hp.toInt
    battleFinalState.self.team(pokemon.name).moves(recover.name).currentPp shouldBe (recover.pp.asInt - 1)

  /*
    DECOMMENTARE QUANDO IL PASO HA SISTEMATO IL CLAMPING DEI DANNI

    test("Heal effect should not exceed maximum HP (clamping)"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 1

    val recover = move named "Recover" withPP 32 withAccuracy 100 withType Normal withEffect (Effect healing 50) as Status
    val battleInitialState: BattleState = createBattleState(recover)
    val nearMaxHp = pokemon.baseStats.hp.toInt - 5
    val damagedState: BattleState = battleInitialState self (_ active (_ currentHp (_ => nearMaxHp)))
    val action: MoveAction = MoveAction(recover)
    val battleFinalState: BattleState = action.execute.foldLeft(damagedState)((state, transformer) => transformer(state))
    battleFinalState.self.team(pokemon.name).currentHp shouldBe pokemon.baseStats.hp.toInt
  */

  test("Damaging move with recoil effect should damage both target and user"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 1

    val doubleEdge = move named "Double edge" withPower 100 withPP 24 withAccuracy 100 withType Normal withEffect (Effect recoil 25) as Physical
    val battleInitialState: BattleState = createBattleState(doubleEdge)
    val action: MoveAction = MoveAction(doubleEdge)
    val battleFinalState: BattleState = action.execute.foldLeft(battleInitialState)((state, transformer) => transformer(state))
    battleFinalState.opponent.team(pokemon.name).currentHp should be < pokemon.baseStats.hp.toInt
    val maxHp = pokemon.baseStats.hp.toInt
    val expectedRecoil = (25 * maxHp) / 100
    battleFinalState.self.team(pokemon.name).currentHp shouldBe (maxHp - expectedRecoil)

  /*
    DECOMMENTARE QUANDO IL PASO HA SISTEMATO IL CLAMPING DEI DANNI

    test("Heal effect should not exceed maximum HP (clamping)"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 1

    val nearMaxHp = pokemon.baseStats.hp.toInt - 5
    val damagedState: BattleState = battleInitialState self (_ active (_ currentHp (_ => nearMaxHp)))
    val action: MoveAction = MoveAction(recover)
    val battleFinalState: BattleState = action.execute.foldLeft(damagedState)((state, transformer) => transformer(state))
    battleFinalState.self.team(pokemon.name).currentHp shouldBe pokemon.baseStats.hp.toInt
  */