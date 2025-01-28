package ulisse.infrastructure.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyle.Border

import java.awt.Color

class BorderStyleTest extends AnyFlatSpec with Matchers:
  val color  = Color.BLACK
  val width  = 2
  val arc    = 20
  val border = Border(color, width, arc)

  "BorderStyle" should "have color, width and arc" in:
    border.color must be(color)
    border.width must be(width)
    border.arc must be(arc)

  "change color" should "have new color" in:
    val newColor  = Color.GREEN
    val newBorder = border.setColor(newColor)
    newBorder.color must be(newColor)
    newBorder.width must be(width)
    newBorder.arc must be(arc)

  "change width" should "have new width" in:
    val newWidth  = 4
    val newBorder = border.setWidth(newWidth)
    newBorder.color must be(color)
    newBorder.width must be(newWidth)
    newBorder.arc must be(arc)

  "change arc" should "have new arc" in:
    val newArc    = 40
    val newBorder = border.setArc(newArc)
    newBorder.color must be(color)
    newBorder.width must be(width)
    newBorder.arc must be(newArc)

  "change all values" should "have new values" in:
    val newColor  = Color.GREEN
    val newWidth  = 4
    val newArc    = 40
    val newBorder = border.setAll(newColor, newWidth, newArc)
    newBorder.color must be(newColor)
    newBorder.width must be(newWidth)
    newBorder.arc must be(newArc)
