package ulisse
import org.scalatest.exceptions.TestFailedException
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.Utils.MatchersUtils.shouldBeBoolean

object Utils:
  object MatchersUtils extends Matchers:
    extension (b1: Boolean)
      def shouldBeBoolean(b2: Boolean): Unit =
        b1 shouldBe b2

class UtilsTest extends AnyWordSpec with Matchers:
  "shouldBeBoolean" should:
    "correctly compare two boolean values" in:
      true shouldBeBoolean true
      false shouldBeBoolean false
      intercept[TestFailedException]:
        false shouldBeBoolean true
        true shouldBeBoolean false
