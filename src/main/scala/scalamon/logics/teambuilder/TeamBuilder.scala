package scalamon.logics.teambuilder

import scalamon.domain.actions.Items
import scalamon.domain.moves.Move
import scalamon.domain.moves.MoveDatabase.allMoves
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.pokemon.pokedex.MyPokedex.*
import scalamon.logics.state.MoveStateModuleImpl.{MoveState, moveInitialState}
import scalamon.logics.state.PlayerStateModuleImpl.{PlayerState, PokemonState, playerInitialState}
import scalamon.logics.state.PokemonStateModuleImpl.pokemonInitialState

/**
 * Module providing the core abstractions and constants for the team-building subsystem.
 * It centralizes the rules governing team composition and provides the structural
 * foundation for various team generation strategies.
 */
object TeamBuilder:
  /** The mandatory number of Pokémon required for a valid player team. */
  val numberOfPokemonPerTeam = 6
  /** The mandatory number of moves required for each Pokémon in the team. */
  val numberOfMovesPerPokemon = 4

  /**
   * A trait representing a generic team-building strategy.
   * This trait implements the "Template Method" design pattern. It defines the
   * fixed algorithm for constructing game states, while delegating the specific
   * selection logic to subclasses through abstract methods.
   *
   * By using this approach, the system ensures that all teams, regardless of how
   * they are generated, adhere to the same validation rules and structural invariants.
   */
  trait TeamBuilder:

    /**
     * Abstract step: chooses a list of Pokémon for the team.
     *
     * @param available The list of all Pokémon available in the domain database.
     * @return A list of Pokémon to be included in the team.
     */
    def choosePokemonTeam(available: List[Pokemon]): List[Pokemon]

    /**
     * Abstract step: chooses a list of moves for a specific Pokémon.
     *
     * @param pokemon The Pokémon for which moves are being selected.
     * @param availableMoves The list of all move available in the domain database.
     * @return A list of moves to be assigned to the Pokémon.
     */
    def chooseMoves(pokemon: Pokemon, availableMoves: List[Move]): List[Move]

    /**
     * Template Method for creating a [[PlayerState]].
     *
     * It orchestrates the team creation process:
     * 1. Invokes the concrete strategy to select Pokémon.
     * 2. Validates the team size invariant.
     * 3. Maps each selected Pokémon to its initial state.
     * 4. Sets the first Pokémon in the list as the active one for battle.
     *
     * @return A fully initialized [[PlayerState]] ready for battle.
     * @throws IllegalArgumentException if the chosen team size is not exactly 6.
     */
    final def buildTeam(playerName: String): PlayerState =
      val chosenPokemonTeam: List[Pokemon] = choosePokemonTeam(allPokemons)
      require(
        chosenPokemonTeam.size == numberOfPokemonPerTeam,
        s"Every player team must contain exactly $numberOfPokemonPerTeam Pokemon"
      )
      val team: Map[String, PokemonState] = chosenPokemonTeam.map(p => p.name -> buildPokemonState(p)).toMap
      playerInitialState(playerName, team, chosenPokemonTeam.head.name, Items.all)      // TODO : item selection

    /**
     * Template Method for creating a [[PokemonState]].
     *
     * It ensures that:
     * 1. Moves are selected according to the specific builder strategy.
     * 2. The move count invariant is strictly enforced.
     * 3. Individual move states are initialized with correct PP values.
     *
     * @param pokemon The species data used as a base for the state.
     * @return A fully initialized [[PokemonState]].
     * @throws IllegalArgumentException if the move count is not exactly 4.
     */
    private final def buildPokemonState(pokemon: Pokemon): PokemonState =
      val chosenMoves: List[Move] = chooseMoves(pokemon, allMoves.toList)
      require(
        chosenMoves.size == numberOfMovesPerPokemon,
        s"Every Pokemon must have exactly $numberOfMovesPerPokemon moves"
      )
      val moveStates: Map[String, MoveState] = chosenMoves.map(m => m.name -> moveInitialState(m)).toMap
      pokemonInitialState(pokemon, moveStates)