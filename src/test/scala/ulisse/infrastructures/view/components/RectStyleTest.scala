package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyleManager._

class RectStyleTest extends AnyFlatSpec with Matchers:
  val defaultRect = rect()

  "default rect style" should "have zero round" in:
    defaultRect.arcTopDx must be(defaultRoundRect)
    defaultRect.arcTopSx must be(defaultRoundRect)
    defaultRect.arcBottomDx must be(defaultRoundRect)
    defaultRect.arcBottomSx must be(defaultRoundRect)

  "round rect style" should "have same round" in:
    val arc        = 10
    val shapeStyle = roundRect(arc)
    shapeStyle.arcTopDx must be(arc)
    shapeStyle.arcTopSx must be(arc)
    shapeStyle.arcBottomDx must be(arc)
    shapeStyle.arcBottomSx must be(arc)

  "change default rect style" should "have new round" in:
    val arc     = 10
    val newRect = defaultRect.setArc(arc)
    newRect.arcTopDx must be(arc)
    newRect.arcTopSx must be(arc)
    newRect.arcBottomDx must be(arc)
    newRect.arcBottomSx must be(arc)
