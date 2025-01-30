package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyleManager._

class RectStyleTest extends AnyFlatSpec with Matchers:
  val defaultRect = rect()

  "default rect style" should "have zero round" in:
    defaultRect.arcWidth must be(defaultRoundRect)
    defaultRect.arcHeight must be(defaultRoundRect)

  "round rect style" should "have same round" in:
    val arc        = 10
    val shapeStyle = roundRect(arc)
    shapeStyle.arcHeight must be(arc)
    shapeStyle.arcHeight must be(arc)

  "change default rect style" should "have new round" in:
    val arc     = 10
    val newRect = defaultRect.setArc(arc)
    newRect.arcWidth must be(arc)
    newRect.arcHeight must be(arc)
