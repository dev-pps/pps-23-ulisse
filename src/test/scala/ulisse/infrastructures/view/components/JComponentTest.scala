package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyleManager.*

import java.awt.Color

class JComponentTest extends AnyFlatSpec with Matchers:
  val styleManager: JStyleManager = apply()
  val text: String                = "Hello"

  val button = JComponent.button(text, styleManager)

  "create JButton" should "with text" in:
    button.text must be(text)

  "change rect" should "have new rect" in:
    val newRect = roundRect(10)
    button.setRect(newRect)
    button.styleManager.rect must be(newRect)

  "change color palette" should "have new color palette" in:
    val newPalette = backgroundPalette(Color.black)
    button.setColorPalette(newPalette)
    button.styleManager.palette must be(newPalette)

  "change border" should "have new border" in:
    val newBorder = colorBorder(Color.black)
    button.setBorder(newBorder)
    button.styleManager.border must be(Some(newBorder))
