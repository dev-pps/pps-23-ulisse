package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyler._

import java.awt.Color

class BorderStyleTest extends AnyFlatSpec with Matchers:
  "default border" should "have color and stroke" in:
    defaultBorder.color must be(defaultColor)
    defaultBorder.stroke must be(defaultStroke)

  "color border" should "have new color" in:
    val newColor  = Color.black
    val newBorder = colorBorder(newColor)
    newBorder.color must be(newColor)
    newBorder.stroke must be(defaultStroke)

  "stroke border" should "have new stroke" in:
    val newStroke = 2
    val newBorder = strokeBorder(newStroke)
    newBorder.color must be(defaultColor)
    newBorder.stroke must be(newStroke)

  "new border" should "have new color and stroke" in:
    val newColor  = Color.black
    val newStroke = 2
    val newBorder = border(newColor, newStroke)
    newBorder.color must be(newColor)
    newBorder.stroke must be(newStroke)
