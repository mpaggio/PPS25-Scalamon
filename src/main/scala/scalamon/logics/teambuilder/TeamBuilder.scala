package scalamon.logics.teambuilder

import scalamon.domain.moves.Move
import scalamon.domain.pokemon.Pokemon
import scalamon.domain.pokemon.pokedex.MyPokedex.*
import scalamon.logics.state.BattleStateImpl.PlayerState
import scalamon.logics.state.MoveStateModuleImpl.moveInitialState
import scalamon.logics.state.PlayerStateModuleImpl.{PokemonState, playerState}
import scalamon.logics.state.PokemonStateModuleImpl.{MoveState, pokemonInitialState}

object TeamBuilder:
  val numberOfPokemonPerTeam = 6
  val numberOfMovesPerPokemon = 4

  trait TeamBuilder:

    def choosePokemonTeam(available: List[Pokemon]): List[Pokemon]

    def chooseMoves(pokemon: Pokemon): List[Move]

    final def buildTeam(): PlayerState =
      val chosenPokemonTeam: List[Pokemon] = choosePokemonTeam(allPokemons)
      require(
        chosenPokemonTeam.size == numberOfPokemonPerTeam,
        s"Every player team must contain exactly $numberOfPokemonPerTeam Pokemon"
      )
      val team: Map[String, PokemonState] = chosenPokemonTeam.map(p => p.name -> buildPokemonState(p)).toMap
      playerState(team, chosenPokemonTeam.head.name)

    private final def buildPokemonState(pokemon: Pokemon): PokemonState =
      val chosenMoves: List[Move] = chooseMoves(pokemon)
      require(
        chosenMoves.size == numberOfMovesPerPokemon,
        s"Every Pokemon must have exactly $numberOfMovesPerPokemon moves"
      )
      val moveStates: Map[String, MoveState] = chosenMoves.map(m => m.name -> moveInitialState(m)).toMap
      pokemonInitialState(pokemon, moveStates)