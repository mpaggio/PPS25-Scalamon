package scalamon.gui

/**
 * A simple State monad implementation.
 * @param run A stateful computation that, given an input state of type S, returns an updated state and a result of type A.
 * @tparam S the type of the state
 * @tparam A the type of the value produced by the stateful computation
 */
case class State[S, A](run: S => (S, A))

/**
 * Companion object for the State monad.
 *
 * It provides basic constructors and combinators for composing
 * stateful computations in a functional style.
 */
object State:
  /**
   * Creates a stateful computation that returns the given value without modifying the state.
   * @param a the value to be returned
   * @tparam S the type of the state
   * @tparam A the type of the value produced by the stateful computation
   * @return the stateful computation that returns the given value without modifying the state
   */
  def unit[S, A](a: A): State[S, A] = State(s => (s, a))

  extension [S, A](m: State[S, A])

    /**
     * Composes two stateful computations, passing the result of the first computation to the second.
     * @param f a function that takes the result of the first computation and returns a new stateful computation
     * @tparam B the type of the value produced by the second stateful computation
     * @return a new stateful computation that represents the composition of the two computations
     */
    def flatMap[B](f: A => State[S, B]): State[S, B] =
      State(s =>
        val (s2, a) = m.run(s)
        f(a).run(s2)
      )

    /**
     * Transforms the result of the stateful computation using the given function, without modifying the state.
     * @param f a function that transforms the result of the stateful computation
     * @tparam B the type of the value produced by the transformed stateful computation
     * @return a new stateful computation that represents the transformation of the result of the original computation
     */
    def map[B](f: A => B): State[S, B] =
      m.flatMap(a => State.unit(f(a)))