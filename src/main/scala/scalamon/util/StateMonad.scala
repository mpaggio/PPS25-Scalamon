package scalamon.util

/**
 * A simple State monad implementation.
 * @param run A stateful computation that, given an input state of type S,
 *            returns an updated state and a result of type A.
 * @tparam S the type of the state
 * @tparam A the type of the value produced by the stateful computation
 */
case class State[S, A](run: S => (S, A))

/**
 * Companion object providing constructors and combinators for composing
 * stateful computations in a functional style.
 */
object State:

  /** Returns the given value without modifying the state. */
  def unit[S, A](a: A): State[S, A] = State(s => (s, a))

  /** Returns the current state as the result. */
  def get[S]: State[S, S] = State(s => (s, s))

  /** Applies a pure transformation to the state. */
  def modify[S](f: S => S): State[S, Unit] = State(s => (f(s), ()))

  /** Extracts a value from the current state without modifying it. */
  def inspect[S, A](f: S => A): State[S, A] = State(s => (s, f(s)))

  /** Runs a stateful computation for each element of the list, collecting the results. */
  def traverse[S, A, B](as: List[A])(f: A => State[S, B]): State[S, List[B]] =
    as.foldRight(unit[S, List[B]](Nil))((a, acc) => f(a).flatMap(b => acc.map(b :: _)))

  extension [S, A](m: State[S, A])

    /** Monadic composition: passes the result of this computation to the next. */
    def flatMap[B](f: A => State[S, B]): State[S, B] =
      State { s =>
        val (s2, a) = m.run(s)
        f(a).run(s2)
      }

    /** Transforms the result without modifying the state. */
    def map[B](f: A => B): State[S, B] =
      m.flatMap(a => unit(f(a)))

    /** Lifts a computation on S into a computation on the first component of a pair state. */
    def onFirst[S2]: State[(S, S2), A] =
      State { case (s, other) =>
        val (s2, a) = m.run(s)
        ((s2, other), a)
      }

    /** Lifts a computation on S into a computation on the second component of a pair state. */
    def onSecond[S1]: State[(S1, S), A] =
      State { case (other, s) =>
        val (s2, a) = m.run(s)
        ((other, s2), a)
      }
