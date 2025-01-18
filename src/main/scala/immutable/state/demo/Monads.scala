package immutable.state.demo

import Streams.*, Stream.*
object Monads:

  trait Monad[M[_]]:
    def unit[A](a: A): M[A]
    extension [A](m: M[A])
      def flatMap[B](f: A => M[B]): M[B]
      def map[B](f: A => B): M[B] = m.flatMap(a => unit(f(a)))

  object Monad:

    // additional general-purpose operations on monads

    def map2[M[_]: Monad, A, B, C](m: M[A], m2: => M[B])(f: (A, B) => C): M[C] =
      m.flatMap(a => m2.map(b => f(a, b)))

    def seq[M[_]: Monad, A, B](m: M[A], m2: => M[B]): M[B] =
      map2(m, m2)((a, b) => b)

    def seqN[M[_]: Monad, A](stream: Stream[M[A]]): M[A] =
      stream match
        case Cons(h, t) => (h(), t()) match
            case (m, Empty()) => m
            case (m, s)       => seq(m, seqN(s))

// ... many others exist
