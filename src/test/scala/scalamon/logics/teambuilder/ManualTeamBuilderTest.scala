package scalamon.logics.teambuilder

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.shouldBe
import scalamon.domain.moves.Move
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.pokemon.pokedex.MyPokedex.allPokemons
import scalamon.logics.teambuilder.ManualTeamBuilder.ManualTeamBuilder
import scalamon.logics.teambuilder.TeamBuilder.numberOfPokemonPerTeam

class ManualTeamBuilderTest extends AnyFunSuite:

  private def pokemonNamed(name: String): Pokemon =
    allPokemons.find(_.name == name).getOrElse(throw new NoSuchElementException(s"Pokemon with name $name not found"))

  private def moveNamed(name: String): Move =
    scalamon.domain.moves.MoveDatabase.allMoves.find(_.name == name).getOrElse(throw new NoSuchElementException(s"Move with name $name not found"))

  test("choosePokemonTeam should return the Pokemon selected by pokemonSelector") {
    val available: List[Pokemon] = List(pokemonNamed("Charmander"), pokemonNamed("Bulbasaur"), pokemonNamed("Squirtle"))
    val expectedTeam: List[Pokemon] = List(pokemonNamed("Bulbasaur"), pokemonNamed("Squirtle"))
    val builder = ManualTeamBuilder(
      pokemonSelector = _ => expectedTeam,
      moveSelector = (_, _) => List.empty[Move])

    val result = builder.choosePokemonTeam(available)
    result shouldBe expectedTeam
  }

  test("chooseMoves should return the moves selected by moveSelector") {
    val pokemon: Pokemon = pokemonNamed("Charmander")
    val availableMoves: List[Move] = List(moveNamed("Flamethrower"), moveNamed("Surf"), moveNamed("Recover"))
    val expectedMoves: List[Move] = List(moveNamed("Flamethrower"), moveNamed("Recover"))
    val builder = ManualTeamBuilder(
      pokemonSelector = _ => List.empty[Pokemon],
      moveSelector = (_, _) => expectedMoves)

    val result = builder.chooseMoves(pokemon, availableMoves)
    result shouldBe expectedMoves
  }

  test("buildTeam should create a player state with the selected Pokemon and moves") {
    val selectedTeam: List[Pokemon] = List(pokemonNamed("Bulbasaur"), pokemonNamed("Squirtle"), pokemonNamed("Charmander"), pokemonNamed("Pikachu"), pokemonNamed("Blastoise"), pokemonNamed("Charizard"))
    val selectedMoves: List[Move] = List(moveNamed("Recover"), moveNamed("Flamethrower"), moveNamed("Fire blast"), moveNamed("Fire punch"))
    val builder = ManualTeamBuilder(
      pokemonSelector = _ => selectedTeam,
      moveSelector = (_, _) => selectedMoves)

    val playerState = builder.buildTeam()

    playerState.team.size shouldBe numberOfPokemonPerTeam
    playerState.team.keySet shouldBe selectedTeam.map(_.name).toSet
    playerState.team.values.foreach { pokemonState =>
      pokemonState.moves.size shouldBe selectedMoves.size
      pokemonState.moves.keySet shouldBe selectedMoves.map(_.name).toSet
    }
  }