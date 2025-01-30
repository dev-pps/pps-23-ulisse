package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyle

import java.awt.Color

class BorderStyle extends AnyFlatSpec with Matchers:
  val defaultBorder = JStyle.border()

  "default border" should "have color and stroke" in:
    defaultBorder.color must be(JStyle.defaultBorderColor)
    defaultBorder.stroke must be(JStyle.defaultStroke)

  "color border" should "have new color" in:
    val newColor  = Color.black
    val newBorder = JStyle.colorBorder(newColor)
    newBorder.color must be(newColor)
    newBorder.stroke must be(JStyle.defaultStroke)

  "complete border" should "have new color and stroke" in:
    val newColor  = Color.black
    val newStroke = 2
    val newBorder = JStyle.completeBorder(newColor, newStroke)
    newBorder.color must be(newColor)
    newBorder.stroke must be(newStroke)

  "change color" should "have new color" in:
    val newColor  = Color.green
    val newBorder = defaultBorder.withColor(newColor)
    newBorder.color must be(newColor)
    newBorder.stroke must be(JStyle.defaultStroke)

  "change stroke" should "have new width" in:
    val newStroke = 4
    val newBorder = defaultBorder.withStroke(newStroke)
    newBorder.color must be(JStyle.defaultBorderColor)
    newBorder.stroke must be(newStroke)

  "change all values" should "have new values" in:
    val newColor  = Color.GREEN
    val newStroke = 4
    val newBorder = defaultBorder.withAll(newColor, newStroke)
    newBorder.color must be(newColor)
    newBorder.stroke must be(newStroke)
