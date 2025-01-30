package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyler._

import java.awt.Color

class BorderStyleTest extends AnyFlatSpec with Matchers:
  val defaultBorder = border()

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

  "complete border" should "have new color and stroke" in:
    val newColor  = Color.black
    val newStroke = 2
    val newBorder = completeBorder(newColor, newStroke)
    newBorder.color must be(newColor)
    newBorder.stroke must be(newStroke)

  "change color" should "have new color" in:
    val newColor  = Color.green
    val newBorder = defaultBorder.withColor(newColor)
    newBorder.color must be(newColor)
    newBorder.stroke must be(defaultStroke)

  "change stroke" should "have new width" in:
    val newStroke = 4
    val newBorder = defaultBorder.withStroke(newStroke)
    newBorder.color must be(defaultColor)
    newBorder.stroke must be(newStroke)

  "change all values" should "have new values" in:
    val newColor  = Color.GREEN
    val newStroke = 4
    val newBorder = defaultBorder.withAll(newColor, newStroke)
    newBorder.color must be(newColor)
    newBorder.stroke must be(newStroke)
