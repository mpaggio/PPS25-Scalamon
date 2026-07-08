package scalamon.logics.state

import scalamon.domain.moves.Accuracy.Accuracy

/**
 * Module abstraction responsible for managing the dynamic state of a Pokémon's move during battle.
 * This module manages runtime information that changes, while the domain [[Move]] remains immutable.
 */
trait MoveStateModule extends StateComponent:
  /** The internal representation of a move's state */
  type MoveState
  /** The reference to the static domain move data. */
  type Move
  /** Type for Power Points (PP) values. */
  type PP
  /** Integration with the [[StateComponent]] architecture. */
  override protected type State = MoveState

  /**
   * Initializes the runtime state for a specific move.
   *
   * @param move The static domain move data.
   * @return The initial MoveState with full PP.
   */
  def moveInitialState(move: Move): MoveState

  /**
   * Higher-order function to transform the current PP.
   *
   * @param f A function defining how to transform the PP value.
   * @return A [[StateTransformer]] (Op) for MoveState
   */
  def currentPp(f: PP => PP): Op

  /**
   * Decrease the current PP by a specific value.
   *
   * @param value The specific value used to decrease current PP.
   * @return A [[StateTransformer]] (Op) for MoveState.
   */
  def decreasePpBy(value: PP): Op

  /**
   * Increase the current PP by a specific value.
   *
   * @param value The specific value used to increase current PP.
   * @return A [[StateTransformer]] (Op) for MoveState.
   */
  def increasePpBy(value: PP): Op

  extension (moveState: MoveState)
    /** Returns the maximum PP allowed for this move. */
    def maxPp: PP
    /** Returns the underlying domain move. */
    def move: Move

/**
 * Concrete implementation of [[MoveStateModule]].
 *
 * It utilizes an immutable case class [[Ms]] to track state and leverages
 * Referential Transparency by returning new state versions via "copy" method.
 */
object MoveStateModuleImpl extends MoveStateModule:
  import scalamon.domain.moves
  import moves.{DamageMove, StatusMove, Accuracy}

  /**
   * Internal data structure for MoveState.
   *
   * @param move Static data reference.
   * @param currentPp Dynamic counter for remaining uses.
   */
  case class Ms(move: Move, currentPp: PP)

  override type MoveState = Ms
  override type Move = moves.Move
  override type PP = Int

  override def moveInitialState(move: Move): MoveState = Ms(move, move.pp.asInt)

  override def currentPp(f: PP => PP): Op = ms => ms.copy(currentPp = f(ms.currentPp).clamped(0, ms.maxPp))

  override def decreasePpBy(value: PP): Op = currentPp(_ - value)

  override def increasePpBy(value: PP): Op = currentPp(_ + value)

  /**
   * Transformation that allows dynamic modification of a move's accuracy during battle.
   * It uses the pattern matching to handle the move hierarchy polymorphically while
   * maintaining immutability.
   *
   * @param f A function transforming the accuracy percentage of the move.
   * @return A [[StateTransformer]] (Op) for MoveState.
   */
  def accuracyPercent(f: Int => Int): Op = ms =>
    val newAccuracyValue = f(ms.move.accuracy.asInt).max(0).min(100)
    ms.move match
    case dm: DamageMove => ms.copy(move = dm.copy(accuracy = Accuracy.accuracyFromPercent(newAccuracyValue)))
    case sm: StatusMove => ms.copy(move = sm.copy(accuracy = Accuracy.accuracyFromPercent(newAccuracyValue)))

  extension (moveState: MoveState)
    def maxPp: PP = moveState.move.pp.asInt
    def move: Move = moveState.move

  /** Utility extension to encapsulate boundary logic. */
  extension (pp: PP)
    private def clamped(min: Int, max: Int): PP = pp.max(min).min(max)