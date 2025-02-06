package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyler.*

class RectStyleTest extends AnyFlatSpec with Matchers:

  "default rect style" should "have zero round" in:
    defaultRect.arc must be(defaultRoundRect)

  "size rect style" should "have same size" in:
    val size = createSize(10, 20)
    val rect = defaultRect.copy(size)
    rect.size must be(size)
    rect.arc must be(defaultRoundRect)

  "round rect style" should "have same round" in:
    val arc  = 10
    val rect = defaultRect.copy(arc = 10)
    rect.arc must be(arc)

  "new rect style" should "have new round" in:
    val size    = createSize(10, 20)
    val padding = createPadding(5, 10)
    val arc     = 10
    val newRect = rect(size, padding, arc)
    newRect.size must be(size)
    newRect.padding must be(padding)
    newRect.arc must be(arc)
