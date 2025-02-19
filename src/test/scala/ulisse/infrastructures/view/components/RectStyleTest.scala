package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.Styles.*

class RectStyleTest extends AnyFlatSpec with Matchers:

  "default rect style" should "have zero round" in:
    defaultRect.size must be(defaultSizeRect)
    defaultRect.padding must be(defaultPaddingRect)
    defaultRect.arc must be(defaultRoundRect)

  "rect with size" should "change size" in:
    val size    = createSize(10, 20)
    val newRect = defaultRect.withSize(size)

    newRect must not be defaultRect
    newRect.size must be(size)

  "rect with width" should "change width" in:
    val width   = 5
    val newRect = defaultRect.withWidth(width)

    newRect must not be defaultRect
    newRect.width must be(Some(width))

  "rect with height" should "change height" in:
    val height  = 5
    val newRect = defaultRect.withHeight(height)

    newRect must not be defaultRect
    newRect.height must be(Some(height))

  "rect with padding" should "change padding" in:
    val padding = createPadding(5, 10)
    val newRect = defaultRect.withPadding(padding)

    newRect must not be defaultRect
    newRect.padding must be(padding)

  "rect with arc" should "change arc" in:
    val arc     = defaultRect.arc + 10
    val newRect = defaultRect.withArc(arc)

    newRect must not be defaultRect
    newRect.arc must be(arc)

  "create a new rect" should "have new size, padding and round" in:
    val width   = 10
    val height  = 20
    val arc     = 10
    val padding = 10
    val newRect = createRect(width, height, padding, arc)

    newRect.size must be(createSize(width, height))
    newRect.padding must be(createPadding(padding, padding))
    newRect.arc must be(arc)
