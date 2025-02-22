package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class BorderStyleTest extends AnyFlatSpec with Matchers:
  "default border" should "have color and stroke" in:
    defaultBorder.stroke must be(defaultStroke)
    defaultBorder.palette must be(defaultPalette)

  "border with stroke" should "change stroke" in:
    val newStroke = 2
    val newBorder = defaultBorder.withStroke(newStroke)

    newBorder must not be defaultBorder
    newBorder.stroke must be(newStroke)

  "create a new border" should "have new color and stroke" in:
    val newStroke = 2

    val newBorder = createBorder(newStroke)
    newBorder.stroke must be(newStroke)
