package ulisse
import org.scalatest.matchers.should.Matchers
object Utils:
  object MatchersUtils extends Matchers:
    extension (b1: Boolean)
      def shouldBeBoolean(b2: Boolean): Unit =
        b1 shouldBe b2
