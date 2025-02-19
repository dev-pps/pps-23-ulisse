package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.Styles.*

import java.awt.Color

class BorderStyleTest extends AnyFlatSpec with Matchers:
  "default border" should "have color and stroke" in:
    defaultBorder.color must be(defaultBorderColor)
    defaultBorder.stroke must be(defaultStroke)

  "border with color" should "change color" in:
    val newColor  = Color.red
    val newBorder = defaultBorder.withColor(newColor)

    newBorder must not be defaultBorder
    newBorder.color must be(newColor)

  "border with stroke" should "change stroke" in:
    val newStroke = 2
    val newBorder = defaultBorder.withStroke(newStroke)

    newBorder must not be defaultBorder
    newBorder.stroke must be(newStroke)

  "create a new border" should "have new color and stroke" in:
    val newColor  = Color.black
    val newStroke = 2
    val newBorder = createBorder(newColor, newStroke)
    newBorder.color must be(newColor)
    newBorder.stroke must be(newStroke)
