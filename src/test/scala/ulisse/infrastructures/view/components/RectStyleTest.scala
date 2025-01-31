package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyler._

class RectStyleTest extends AnyFlatSpec with Matchers:

  "default rect style" should "have zero round" in:
    defaultRect.arcWidth must be(defaultRoundRect)
    defaultRect.arcHeight must be(defaultRoundRect)

  "round rect style" should "have same round" in:
    val arc        = 10
    val shapeStyle = rect(arc)
    shapeStyle.arcHeight must be(arc)
    shapeStyle.arcHeight must be(arc)
