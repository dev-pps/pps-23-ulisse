package immutable.state.demo.immutableApplication

object Streams:

  //  import Sequences.*

  enum Stream[A]:
    case Empty()
    case Cons(head: () => A, tail: () => Stream[A])

  object Stream:

    def empty[A](): Stream[A] = Empty()

    def cons[A](hd: => A, tl: => Stream[A]): Stream[A] =
      lazy val head = hd
      lazy val tail = tl
      Cons(() => head, () => tail)

    extension [A](stream: Stream[A])
      def map[B](f: A => B): Stream[B] = stream match
        case Cons(head, tail) => cons(f(head()), tail().map(f))
        case _                => Empty()

    extension [A](stream: Stream[A])
      def filter(pred: A => Boolean): Stream[A] = stream match
        case Cons(head, tail) if (pred(head())) => cons(head(), tail().filter(pred))
        case Cons(head, tail)                   => tail().filter(pred)
        case _                                  => Empty()

    extension [A](stream: Stream[A])
      def take(n: Int): Stream[A] = (stream, n) match
        case (Cons(head, tail), n) if n > 0 => cons(head(), tail().take(n - 1))
        case _                              => Empty()

    def iterate[A](init: => A)(next: A => A): Stream[A] =
      cons(init, iterate(next(init))(next))

    def generate[A](supplier: () => A): Stream[A] =
      cons(supplier(), generate(supplier))

  end Stream
