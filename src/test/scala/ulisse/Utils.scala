package ulisse
import org.scalatest.Assertions.fail
import ulisse.Utils.TestUtility.getOrFail
import org.scalatest.exceptions.TestFailedException
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.Utils.MatchersUtils.shouldBeBoolean
import ulisse.entities.station.Station
import ulisse.utils.Errors.BaseError

object Utils:
  object MatchersUtils extends Matchers:
    extension (b1: Boolean)
      def shouldBeBoolean(b2: Boolean): Unit =
        b1 shouldBe b2

  object TestUtility:
    extension [E, R](item: Either[E, R])
      def in(test: R => Unit): Unit =
        item match
          case Left(e)  => fail(s"initialization error: $e")
          case Right(t) => test(t)

      def getOrFail: R =
        item match
          case Left(e: E) => fail(s"initialization error: $e")
          case Right(t)   => t

    extension [E, R](f: Either[E, R])
      def and[E1, R1](g: Either[E1, R1])(test: (R, R1) => Unit): Unit =
        f in: r =>
          g in: e =>
            test(r, e)

class UtilsTest extends AnyWordSpec with Matchers:
  "shouldBeBoolean" should:
    "correctly compare two boolean values" in:
      true shouldBeBoolean true
      false shouldBeBoolean false
      intercept[TestFailedException]:
        false shouldBeBoolean true
        true shouldBeBoolean false

  "getOrFail" should:
    "return the value if it's a Right" in:
      val item: Either[BaseError, Int] = Right(1)
      item.getOrFail shouldBe 1

    "throw an exception if it's a Left" in:
      intercept[TestFailedException]:
        Left("error").getOrFail
