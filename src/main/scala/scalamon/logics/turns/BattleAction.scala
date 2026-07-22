package scalamon.logics.turns

/**
 * Identifier of a Pokémon involved in a battle turn.
 *
 * This opaque type provides a domain-specific identifier for a Pokémon
 * while hiding its underlying string representation.
 */
opaque type PokemonRef = String

object PokemonRef:
  /**
   * Creates a Pokémon identifier from its raw string representation.
   *
   * @param value
   *   the underlying identifier value
   * @return
   *   a [[PokemonRef]] wrapping the provided value
   */
  def apply(value: String): PokemonRef = value

  extension (pokemonRef: PokemonRef)
    /**
     * Returns the underlying string representation of this Pokémon identifier.
     *
     * @return
     *   the raw identifier value
     */
    def value: String = pokemonRef

/**
 * Identifier of a move selected during a battle turn.
 *
 * This opaque type provides a domain-specific identifier for a move
 * while hiding its underlying string representation.
 */
opaque type MoveRef = String

object MoveRef:
  /**
   * Creates a move identifier from its raw string representation.
   *
   * @param value
   *   the underlying identifier value
   * @return
   *   a [[MoveRef]] wrapping the provided value
   */
  def apply(value: String): MoveRef = value

  extension (moveRef: MoveRef)
    /**
     * Returns the underlying string representation of this move identifier.
     *
     * @return
     *   the raw identifier value
     */
    def value: String = moveRef

/**
 * Action chosen by a trainer for a battle turn.
 *
 * Each action exposes a priority value that can be used to determine
 * resolution order during turn processing.
 */
trait BattleAction:
  /**
   * Priority associated with this action.
   *
   * Higher-priority actions can be resolved before lower-priority ones,
   * depending on the battle rules.
   *
   * @return
   *   the priority of this action
   */
  def priority: Int

/**
 * Action that executes a move during the current turn.
 *
 * @param move
 *   the move selected for execution
 * @param priority
 *   the priority associated with the move execution
 */
case class UseMove(move: MoveRef, priority: Int = 0) extends BattleAction

/**
 * Action that switches the currently active Pokémon.
 *
 * @param to
 *   the identifier of the Pokémon to switch in
 */
case class SwitchPokemon(to: PokemonRef) extends BattleAction:
  /**
   * Priority associated with this switch action.
   *
   * @return
   *   the priority of the action
   */
  def priority: Int = 100

/**
 * Action that uses an item during the turn.
 *
 * @param item
 *   the identifier or name of the item to use
 */
case class UseItem(item: String) extends BattleAction:
  /**
   * Priority associated with this item usage action.
   *
   * @return
   *   the priority of the action
   */
  def priority: Int = 100