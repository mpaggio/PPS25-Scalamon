package scalamon.logics.teambuilder

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.shouldBe
import scalamon.app.GameConfig.TeamSize
import scalamon.database.MoveDatabase
import scalamon.domain.actions.Items.{Item, allItems}
import scalamon.domain.moves.Move
import scalamon.domain.pokemon.Pokemon
import scalamon.database.MyPokedex.allPokemons
import scalamon.logics.teambuilder.ManualTeamBuilder.ManualTeamBuilder

class ManualTeamBuilderTest extends AnyFunSuite:

  private def pokemonNamed(name: String): Pokemon =
    allPokemons.find(_.name == name).getOrElse(throw new NoSuchElementException(s"Pokemon with name $name not found"))

  private def moveNamed(name: String): Move =
    MoveDatabase.allMoves.find(_.name == name).getOrElse(throw new NoSuchElementException(s"Move with name $name not found"))

  private def itemNamed(name: String): Item =
    allItems.find(_.name == name).getOrElse(throw new NoSuchElementException(s"Item with name $name not found"))

  test("choosePokemonTeam should return the Pokemon selected by pokemonSelector") {
    val available: List[Pokemon] = List(pokemonNamed("Charmander"), pokemonNamed("Bulbasaur"), pokemonNamed("Squirtle"))
    val expectedTeam: List[Pokemon] = List(pokemonNamed("Bulbasaur"), pokemonNamed("Squirtle"))
    val builder = ManualTeamBuilder(
      choosePokemonTeam = (_, _) => expectedTeam,
      chooseMoves = (_, _, _) => List.empty[Move],
      chooseItems = (_, _) => Set.empty
    )

    val result = builder.choosePokemonTeam(available, expectedTeam.size)
    result shouldBe expectedTeam
  }

  test("chooseMoves should return the moves selected by moveSelector") {
    val pokemon: Pokemon = pokemonNamed("Charmander")
    val availableMoves: List[Move] = List(moveNamed("Flamethrower"), moveNamed("Surf"), moveNamed("Recover"))
    val expectedMoves: List[Move] = List(moveNamed("Flamethrower"), moveNamed("Recover"))
    val builder = ManualTeamBuilder(
      choosePokemonTeam = (_, _) => List.empty[Pokemon],
      chooseMoves = (_, _, _) => expectedMoves,
      chooseItems = (_, _) => Set.empty
    )

    val result = builder.chooseMoves(pokemon, availableMoves, expectedMoves.size)
    result shouldBe expectedMoves
  }

  test("buildTeam should create a player state with the selected Pokemon and moves") {
    val selectedTeam: List[Pokemon] = List(pokemonNamed("Bulbasaur"), pokemonNamed("Squirtle"), pokemonNamed("Charmander"), pokemonNamed("Pikachu"), pokemonNamed("Blastoise"), pokemonNamed("Charizard"))
    val selectedMoves: List[Move] = List(moveNamed("Recover"), moveNamed("Flamethrower"), moveNamed("Fire blast"), moveNamed("Fire punch"))
    val selectedItems: Set[Item] = Set(itemNamed("potion"), itemNamed("fresh_water"))
    val builder = ManualTeamBuilder(
      choosePokemonTeam = (_, _) => selectedTeam,
      chooseMoves = (_, _, _) => selectedMoves,
      chooseItems = (_, _) => selectedItems
    )

    val playerState = builder.buildTeam("Player1")

    playerState.team.size shouldBe TeamSize
    playerState.team.keySet shouldBe selectedTeam.map(_.name).toSet
    playerState.team.values.foreach { pokemonState =>
      pokemonState.moves.size shouldBe selectedMoves.size
      pokemonState.moves.keySet shouldBe selectedMoves.map(_.name).toSet
    }
    playerState.items shouldBe selectedItems
  }