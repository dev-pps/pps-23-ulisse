package ulisse

import org.scalatest.Assertions.fail

object TestUtility:
  extension [E, R](item: Either[E, R])
    def in(test: R => Unit): Unit =
      item match
        case Left(e)  => fail(s"initialization error: $e")
        case Right(t) => test(t)

  extension [E, R](f: Either[E, R])
    def and[E1, R1](g: Either[E1, R1])(test: (R, R1) => Unit): Unit =
      f in: r =>
        g in: e =>
          test(r, e)
