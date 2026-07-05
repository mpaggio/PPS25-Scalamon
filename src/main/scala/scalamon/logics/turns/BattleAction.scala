package scalamon.logics.turns

import scalamon.domain.actions.SwitchAction
import scalamon.logics.state.StateTransformerModule

/**
 * Opaque identifier of a trainer involved in a battle turn.
 */
opaque type TrainerId = String
object TrainerId:
  /**
   * Creates a [[TrainerId]] from its string representation.
   *
   * @param value
   * the underlying trainer identifier
   * @return
   * a trainer identifier
   */
  def apply(value: String): TrainerId = value

  extension(trainerId: TrainerId)
    /**
     * Extracts the underlying string value of this trainer identifier.
     *
     * @return
     * the raw trainer identifier
     */
    def value: String = trainerId

/**
 * Opaque identifier of a Pokémon involved in a battle turn.
 */
opaque type PokemonRef = String
object PokemonRef:
  /**
   * Creates a [[PokemonRef]] from its string representation.
   *
   * @param value
   * the underlying Pokémon reference
   * @return
   * a Pokémon reference
   */
  def apply(value: String): PokemonRef = value

  extension(pokemonRef: PokemonRef)
    /**
     * Extracts the underlying string value of this Pokémon reference.
     *
     * @return
     *   the raw Pokémon reference
     */
    def value: String = pokemonRef

/**
 * Opaque identifier of a move selected during a battle turn.
 */
opaque type MoveRef = String
object MoveRef:
  /**
   * Creates a [[MoveRef]] from its string representation.
   *
   * @param value
   * the underlying move reference
   * @return
   * a move reference
   */
  def apply(value: String): MoveRef = value

  extension(moveRef: MoveRef)
    /**
     * Extracts the underlying string value of this move reference.
     *
     * @return
     *   the raw move reference
     */
    def value: String = moveRef

/**
 * Action selected by a trainer during a battle turn.
 */
trait BattleAction:
  def priority: Int

case class UseMove(move: MoveRef, priority: Int = 0) extends BattleAction

case class SwitchPokemon(to: PokemonRef) extends BattleAction:
  def priority: Int = 0
