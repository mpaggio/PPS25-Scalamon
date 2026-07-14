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
import scalamon.logics.state.StateTransformerModuleImpl.StateTransformer
import scalamon.logics.state.StatsStateModuleImpl.*
import Accuracy.*
import scalamon.domain.weather.Weather.*
import scalamon.logics.weather.WeatherSystem.given

class MoveActionTest extends org.scalatest.funsuite.AnyFunSuite:

  val pokemon: PokemonSpecies = allPokemons.last
  val swift: DamagingMove = move named "Swift" withPower 60 withPP 32 withAccuracy 100 withType Normal as Special
  val initialMoves = Map(swift.name -> moveInitialState(swift))
  val pokemonStartingState: PokemonState = pokemonInitialState(pokemon, initialMoves)
  val battleStartingState: BattleState = battleState(
    userPokemon = playerInitialState("Player1", Map.from(List(pokemon.name -> pokemonStartingState)), pokemon.name),
    enemyPokemon = playerInitialState("Player2", Map.from(List(pokemon.name -> pokemonStartingState)), pokemon.name)
  )

  test("A damaging move action should decrease its Power Points (PP) after every single use"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 1

    val action: MoveAction = MoveAction(swift)
    val state1 = action(battleStartingState)
    state1.self.team(pokemon.name).moveState(swift.name).currentPp shouldBe 31
    val state2 = action(state1)
    state2.self.team(pokemon.name).moveState(swift.name).currentPp shouldBe 30

  test("Damaging move action with 100% accuracy should always decrease enemy HP and consume PP"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 1

    val battleEndingState =
      MoveAction(swift)(battleStartingState)
    battleEndingState.opponent.team(pokemon.name).currentHp should be < pokemon.baseStats.hp.toInt
    battleEndingState.self.team(pokemon.name).moveState(swift.name).currentPp shouldBe 31

  test("Damaging move action with 0% accuracy should not decrease enemy HP but must consume PP"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 101

    val battleEndingState =
      MoveAction(swift)(battleStartingState)
    battleEndingState.opponent.team(pokemon.name).currentHp shouldBe pokemon.baseStats.hp.toInt
    battleEndingState.self.team(pokemon.name).moves(swift.name).currentPp shouldBe 31

  private def createBattleState(move: Move): BattleState =
    val moveState = Map(move.name -> moveInitialState(move))
    val pokemonState = pokemonInitialState(pokemon, moveState)
    battleState(
      userPokemon = playerInitialState("Player1", Map.from(List(pokemon.name -> pokemonState)), pokemon.name),
      enemyPokemon = playerInitialState("Player2", Map.from(List(pokemon.name -> pokemonState)), pokemon.name)
    )

  test("A status move should decrease its Power Points (PP) after every single use"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 1

    val recover = move named "Recover" withPP 32 withAccuracy 100 withType Normal withEffect (Effect healing 50) as Status
    val battleInitialState: BattleState = createBattleState(recover)
    val action: MoveAction = MoveAction(recover)
    val state1 = action(battleInitialState)
    state1.self.team(pokemon.name).moveState(recover.name).currentPp shouldBe 31
    val state2 = action(state1)
    state2.self.team(pokemon.name).moveState(recover.name).currentPp shouldBe 30

  test("Status move with 0% accuracy should not have effects but must consume PP"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 101

    val recover = move named "Recover" withPP 32 withAccuracy 100 withType Normal withEffect (Effect healing 50) as Status
    val halfHp = pokemon.baseStats.hp.toInt / 2
    val battleInitialState: BattleState = createBattleState(recover)
    val damagedState: BattleState = self ( active ( currentHp (_ => halfHp)))(battleInitialState)
    val battleEndingState = MoveAction(recover)(damagedState)
    battleEndingState.self.team(pokemon.name).currentHp shouldBe halfHp
    battleEndingState.self.team(pokemon.name).moves(recover.name).currentPp shouldBe 31

  test("Status move with healing effect should heal the user and consume PP"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 1

    val recover = move named "Recover" withPP 32 withAccuracy 100 withType Normal withEffect (Effect healing 50) as Status
    val battleInitialState: BattleState = createBattleState(recover)
    val halfHp = pokemon.baseStats.hp.toInt / 2
    val damagedState: BattleState = self ( active ( currentHp (_ => halfHp)))(battleInitialState)
    val action: MoveAction = MoveAction(recover)
    val battleFinalState: BattleState = action(damagedState)
    battleFinalState.self.team(pokemon.name).currentHp shouldBe pokemon.baseStats.hp.toInt
    battleFinalState.self.team(pokemon.name).moves(recover.name).currentPp shouldBe (recover.pp.asInt - 1)

  test("Damaging move with recoil effect should damage both target and user"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 1

    val doubleEdge = move named "Double edge" withPower 100 withPP 24 withAccuracy 100 withType Normal withEffect (Effect recoil 25) as Physical
    val battleInitialState: BattleState = createBattleState(doubleEdge)
    val action: MoveAction = MoveAction(doubleEdge)
    val battleFinalState: BattleState = action(battleInitialState)
    battleFinalState.opponent.team(pokemon.name).currentHp should be < pokemon.baseStats.hp.toInt
    val maxHp = pokemon.baseStats.hp.toInt
    val expectedRecoil = (25 * maxHp) / 100
    battleFinalState.self.team(pokemon.name).currentHp shouldBe (maxHp - expectedRecoil)

  test("Status move with composable effect should lead to the correct effects on the state"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 1

    val transformation1: StateTransformer = opponent( bench( currentHp( decrease(10))))
    val transformation2: StateTransformer = opponent( active( modifyStats( speed( decrease(2)))))
    val transformation3: StateTransformer = self(active( heal( 50)))
    val composedEffect: StateTransformer = transformation1.andThen(transformation2).andThen(transformation3)
    val effect: MoveEffect = Effect transformingBy composedEffect
    val personalizedMove = move named "Composed move" withPP 32 withAccuracy 100 withType Normal withEffect effect as Status
    val battleInitialState: BattleState = createBattleState(personalizedMove)
    val damagedState: BattleState = self( active( currentHp(_ => 50)))(battleInitialState)
    val action: MoveAction = MoveAction(personalizedMove)
    val battleFinalState: BattleState = action(damagedState)

    battleFinalState.opponent.team.filterNot(_._1 == battleFinalState.opponent.activeId).values.foreach(pS => pS.currentHp shouldBe (100 - 10))
    battleFinalState.opponent.getActive.modifiedStats.speed shouldBe (battleFinalState.opponent.getActive.species.baseStats.speed.toInt - 2)
    battleFinalState.self.getActive.currentHp shouldBe math.min(100, battleFinalState.self.getActive.species.baseStats.hp.toInt)

  test("Fog should reduce move accuracy"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 90

    val fogBattle = battleStartingState.copy(weather = Fog)
    val battleEndingState = MoveAction(swift)(fogBattle)
    battleEndingState.opponent.team(pokemon.name).currentHp shouldBe pokemon.baseStats.hp.toInt

  test("Rain should make Electric moves ignore accuracy"):
    import scalamon.logics.state.DamagePolicy.Easy.given
    given ProbabilityRoll = () => 100

    val thunder = move named "Thunder" withPower 120 withPP 16 withAccuracy 70 withType Electric as Special
    val rainBattle = createBattleState(thunder).copy(weather = Rain)
    val battleEndingState = MoveAction(thunder)(rainBattle)
    battleEndingState.opponent.team(pokemon.name).currentHp should be < pokemon.baseStats.hp.toInt