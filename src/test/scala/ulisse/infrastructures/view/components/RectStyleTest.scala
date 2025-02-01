package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyler.*

class RectStyleTest extends AnyFlatSpec with Matchers:

  "default rect style" should "have zero round" in:
    defaultRect.arcWidth must be(defaultRoundRect)
    defaultRect.arcHeight must be(defaultRoundRect)

  "size rect style" should "have same size" in:
    val size = Size(10, 20)
    val rect = sizeRect(size)
    rect.size must be(Some(size))
    rect.arcWidth must be(defaultRoundRect)
    rect.arcHeight must be(defaultRoundRect)

  "round rect style" should "have same round" in:
    val arc  = 10
    val rect = roundRect(arc)
    rect.arcHeight must be(arc)
    rect.arcHeight must be(arc)

  "new rect style" should "have new round" in:
    val size    = Size(10, 20)
    val arc     = 10
    val newRect = rect(size, arc)
    newRect.size must be(Some(size))
    newRect.arcHeight must be(arc)
    newRect.arcHeight must be(arc)
