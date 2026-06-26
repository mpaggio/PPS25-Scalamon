package scalamon.logics.turns

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
enum BattleAction:
  /**
   * Uses a move against an opposing Pokémon.
   *
   * @param trainerId
   * the trainer performing the action
   * @param attacking
   * the attacking Pokémon
   * @param defending
   * the defending Pokémon
   * @param move
   * the selected move
   * @param priority
   * the action priority used to resolve execution order
   */
  case UseMove(
              trainerId: TrainerId,
              attacking: PokemonRef,
              defending: PokemonRef,
              move: MoveRef,
              priority: Int
              )
  /**
   * Switches the currently active Pokémon with another one.
   *
   * @param trainerId
   * the trainer performing the action
   * @param from
   * the Pokémon being switched out
   * @param to
   * the Pokémon being switched in
   * @param priority
   * the action priority used to resolve execution order
   */
  case SwitchPokemon(
                    trainerId: TrainerId,
                    from: PokemonRef,
                    to: PokemonRef,
                    priority: Int
                    )
