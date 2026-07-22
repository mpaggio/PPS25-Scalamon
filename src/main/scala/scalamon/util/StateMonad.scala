package scalamon.util

/**
 * A simple State monad implementation.
 * @param run A stateful computation that, given an input state of type S,
 *            returns an updated state and a result of type A.
 * @tparam S the type of the state
 * @tparam A the type of the value produced by the stateful computation
 */
case class StateMonad[S, A](run: S => (S, A))

/**
 * Companion object providing constructors and combinators for composing
 * stateful computations in a functional style.
 */
object StateMonad:

  /** Returns the given value without modifying the state. */
  def unit[S, A](a: A): StateMonad[S, A] = StateMonad(s => (s, a))

  /** Returns the current state as the result. */
  def get[S]: StateMonad[S, S] = StateMonad(s => (s, s))

  /** Applies a pure transformation to the state. */
  def modify[S](f: S => S): StateMonad[S, Unit] = StateMonad(s => (f(s), ()))

  /** Extracts a value from the current state without modifying it. */
  def inspect[S, A](f: S => A): StateMonad[S, A] = StateMonad(s => (s, f(s)))

  /** Runs a stateful computation for each element of the list, collecting the results. */
  def traverse[S, A, B](as: List[A])(f: A => StateMonad[S, B]): StateMonad[S, List[B]] =
    as.foldRight(unit[S, List[B]](Nil))((a, acc) => f(a).flatMap(b => acc.map(b :: _)))

  extension [S, A](m: StateMonad[S, A])

    /** Monadic composition: passes the result of this computation to the next. */
    def flatMap[B](f: A => StateMonad[S, B]): StateMonad[S, B] = StateMonad: s =>
      val (newS, a) = m.run(s)
      f(a).run(newS)

    /** Transforms the result without modifying the state. */
    def map[B](f: A => B): StateMonad[S, B] = m.flatMap(a => unit(f(a)))

    /** Lifts a computation on S into a computation on the first component of a pair state. */
    def onFirst[S2]: StateMonad[(S, S2), A] = StateMonad: (s, other) =>
      val (newS, a) = m.run(s)
      ((newS, other), a)

    /** Lifts a computation on S into a computation on the second component of a pair state. */
    def onSecond[S1]: StateMonad[(S1, S), A] = StateMonad: (other, s) =>
      val (newS, a) = m.run(s)
      ((other, newS), a)

