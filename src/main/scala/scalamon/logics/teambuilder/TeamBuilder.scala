package scalamon.logics.teambuilder

import scalamon.domain.actions.Items.{Item, allItems}
import scalamon.domain.moves.Move
import scalamon.database.MoveDatabase.allMoves
import scalamon.domain.pokemon.Pokemon
import scalamon.database.MyPokedex.*
import scalamon.logics.state.MoveStateModuleImpl.moveInitialState
import scalamon.logics.state.PlayerStateModuleImpl.{PlayerState, PokemonState, playerInitialState}
import scalamon.logics.state.PokemonStateModuleImpl.pokemonInitialState
import scalamon.app.GameConfig.*

/**
 * Module providing the core abstractions and constants for the team-building subsystem.
 * It centralizes the rules governing team composition and provides the structural
 * foundation for various team generation strategies.
 */
object TeamBuilder:

  type PokemonSelector = (List[Pokemon], Int) => List[Pokemon]
  type MoveSelector = (Pokemon, List[Move], Int) => List[Move]
  type ItemSelector = (Set[Item], Int) => Set[Item]

  /**
   * A trait representing a generic team-building strategy.
   * This trait implements the "Template Method" design pattern: it defines the
   * fixed algorithm for constructing game states, while delegating the specific
   * selection logic to the abstract steps below.
   *
   * The steps are declared as function-valued members (rather than methods
   * with parameter lists) so that a concrete strategy can be plain data: a
   * case class whose fields *are* the steps (see [[ManualTeamBuilder]]).
   */
  trait TeamBuilder:

    /** Abstract step: chooses the Pokémon for the team. */
    def choosePokemonTeam: PokemonSelector

    /** Abstract step: chooses the moves for a specific Pokémon. */
    def chooseMoves: MoveSelector

    /** Abstract step: chooses the items the player will carry into battle. */
    def chooseItems: ItemSelector

    /**
     * Template Method for creating a [[PlayerState]]:
     * invokes the concrete steps for Pokémon and items, enforces the
     * composition invariants, maps each Pokémon to its initial state and
     * sets the first one as the active lead.
     *
     * @throws IllegalArgumentException if any invariant is violated.
     */
    final def buildTeam(playerName: String): PlayerState =
      val chosenPokemonTeam = choosePokemonTeam(allPokemons, TeamSize)
      require(
        chosenPokemonTeam.size == TeamSize,
        s"Every player team must contain exactly $TeamSize Pokemon"
      )
      val team = chosenPokemonTeam.map(p => p.name -> buildPokemonState(p)).toMap

      val items = chooseItems(allItems, ItemsPerPlayer)

      playerInitialState(playerName, team, chosenPokemonTeam.head.name, items)

    /**
     * Template Method for creating a [[PokemonState]]: moves are selected by
     * the concrete step, the move-count invariant is enforced, and the move
     * states are initialized with the correct PP values.
     */
    private final def buildPokemonState(pokemon: Pokemon): PokemonState =
      val chosenMoves = chooseMoves(pokemon, allMoves.toList, MovesPerPokemon)
      require(
        chosenMoves.size == MovesPerPokemon,
        s"Every Pokemon must have exactly $MovesPerPokemon moves"
      )
      val moveStates = chosenMoves.map(m => m.name -> moveInitialState(m)).toMap
      pokemonInitialState(pokemon, moveStates)