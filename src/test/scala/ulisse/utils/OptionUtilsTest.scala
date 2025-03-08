package ulisse.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.utils.OptionUtils.when

class OptionUtilsTest extends AnyWordSpec with Matchers:
  "when" should:
    "return Some if the condition is true" in:
      1 when true shouldBe Some(1)

    "return None if the condition is false" in:
      1 when false shouldBe None
