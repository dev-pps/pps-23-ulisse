package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyler.*

import java.awt.Color

class JComponentTest extends AnyFlatSpec with Matchers:
  val styleManager: JStyler = styler()
  val text: String          = "Hello"

  val button = JComponent.button(text, styleManager)

  "create JButton" should "with text" in:
    button.text must be(text)

  "change rect" should "have new rect" in:
    val newRect = roundRect(10)
    button.setRect(newRect)
    button.styler.rect must be(newRect)

  "change color palette" should "have new color palette" in:
    val newPalette = backgroundPalette(Color.black)
    button.setColorPalette(newPalette)
    button.styler.palette must be(newPalette)

  "change border" should "have new border" in:
    val newBorder = colorBorder(Color.black)
    button.setBorder(newBorder)
    button.styler.border must be(Some(newBorder))
