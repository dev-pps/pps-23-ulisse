package ulisse.utils

/** Create a [[Pair]] to represent a pair of values of the same type. */
trait Pair[T]:
  /** The first value of the pair. */
  def a: T

  /** The second value of the pair. */
  def b: T

  /** Set the first value of the pair in [[newA]]. */
  def withA(newA: T): Pair[T]

  /** Set the second value of the pair in [[newB]]. */
  def withB(newB: T): Pair[T]

object Pair:
  /** Create a [[Pair]] with the values [[a]] and [[b]]. */
  def apply[T](a: T, b: T): Pair[T] = PairImpl(a, b)

  /** Implementation of a [[Pair]] of values of the same type. */
  case class PairImpl[T](a: T, b: T) extends Pair[T]:
    def withA(newA: T): PairImpl[T] = copy(a = newA)
    def withB(newB: T): PairImpl[T] = copy(b = newB)

  /** Methods to perform operations on a [[PairImpl]] of [[Numeric]] values. */
  extension [T: Numeric](pair: Pair[T])
    /** Subtract other from the pair. */
    def minus(other: Pair[T])(using numeric: Numeric[T]): Pair[T] =
      PairImpl(numeric.minus(pair.a, other.a), numeric.minus(pair.b, other.b))

    /** Add other to the pair. */
    def plus(other: Pair[T])(using numeric: Numeric[T]): Pair[T] =
      PairImpl(numeric.plus(pair.a, other.a), numeric.plus(pair.b, other.b))

    /** Multiply other to the pair. */
    def times(other: Pair[T])(using numeric: Numeric[T]): Pair[T] =
      PairImpl(numeric.times(pair.a, other.a), numeric.times(pair.b, other.b))
