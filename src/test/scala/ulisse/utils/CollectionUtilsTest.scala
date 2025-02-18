package ulisse.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.utils.CollectionUtils.*

class CollectionUtilsTest extends AnyWordSpec with Matchers:

  "updateWhen" should:
    "update all items that satisfy the condition" in:
      List(1, 2, 3, 4, 5).updateWhen(_ % 2 == 0)(_ + 1) shouldBe List(1, 3, 3, 5, 5)

  "updateFirstWhen" should:
    "update the first item that satisfies the condition" in:
      List(1, 2, 3, 4, 5).updateFirstWhen(_ % 2 == 0)(_ + 1) shouldBe List(1, 3, 3, 4, 5)
