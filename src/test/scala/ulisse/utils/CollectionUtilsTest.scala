package ulisse.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.utils.CollectionUtils.*

class CollectionUtilsTest extends AnyWordSpec with Matchers:

  "updateWhen" should:
    "update all items that satisfy the condition" in:
      List(1, 2, 3, 4, 5).updateWhen(_ % 2 == 0)(_ + 1) shouldBe List(1, 3, 3, 5, 5)

  "updateWhenWithEffects" should:
    "update all items that satisfy the condition with effects" in:
      List(1, 2, 3, 4, 5).updateWhenWithEffects(_ % 2 == 0)(i => Option(i + 1)) shouldBe Some(List(1, 3, 3, 5, 5))
      List(1, 2, 3, 4, 5).updateWhenWithEffects(_ % 2 == 0)(i => None) shouldBe None
