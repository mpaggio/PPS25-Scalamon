package scalamon.logics.turns

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import scalamon.domain.moves.Move
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.pokemon.pokedex.MyPokedex.allPokemons
import scalamon.logics.teambuilder.ManualTeamBuilder.ManualTeamBuilder
import scalamon.logics.teambuilder.TeamBuilder.{numberOfMovesPerPokemon, numberOfPokemonPerTeam}

class BattleSetupTest extends AnyFunSuite:

  private def pokemonNamed(name: String): Pokemon =
    allPokemons.find(_.name == name).getOrElse(throw new NoSuchElementException(s"Pokemon with name $name not found"))

  private def moveNamed(name: String): Move =
    scalamon.domain.moves.MoveDatabase.allMoves.find(_.name == name).getOrElse(throw new NoSuchElementException(s"Move with name $name not found"))

  test("setupBattle should create an initial battle state with player1 as self and player2 as opponent") {
    val team1 = List(
      pokemonNamed("Bulbasaur"),
      pokemonNamed("Charmander"),
      pokemonNamed("Squirtle"),
      pokemonNamed("Venusaur"),
      pokemonNamed("Charizard"),
      pokemonNamed("Blastoise")
    )

    val team2 = List(
      pokemonNamed("Pikachu"),
      pokemonNamed("Gengar"),
      pokemonNamed("Mewtwo"),
      pokemonNamed("Jolteon"),
      pokemonNamed("Hypno"),
      pokemonNamed("Arbok")
    )

    val builder1 = ManualTeamBuilder(
      pokemonSelector = _ => team1,
      moveSelector = (_, _) => List(moveNamed("Body slam"), moveNamed("Hyper beam"), moveNamed("Double edge"), moveNamed("Slash"))
    )

    val builder2 = ManualTeamBuilder(
      pokemonSelector = _ => team2,
      moveSelector = (_, _) => List(moveNamed("Swift"), moveNamed("Strength"), moveNamed("Recover"), moveNamed("Ember"))
    )

    val state = BattleSetup.setupBattle(builder1, builder2)

    state.self.team.size shouldBe numberOfPokemonPerTeam
    state.opponent.team.size shouldBe numberOfPokemonPerTeam

    state.self.team.keySet shouldBe team1.map(_.name).toSet
    state.opponent.team.keySet shouldBe team2.map(_.name).toSet

    state.self.activeId shouldBe team1.head.name
    state.opponent.activeId shouldBe team2.head.name

    state.self.team.values.foreach(_.moves.size shouldBe numberOfMovesPerPokemon)
    state.opponent.team.values.foreach(_.moves.size shouldBe numberOfMovesPerPokemon)

    state.passiveEffects shouldBe empty
    state.opponent.flags.isSwitchBlocked shouldBe false
    state.self.flags.weatherSuppressed shouldBe false
    state.self.flags.flashFireActive shouldBe false
    state.self.flags.magicGuardActive shouldBe false
    state.self.flags.lastOpponentMove shouldBe None

  }