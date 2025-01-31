package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyler.*

import java.awt.Color

class JComponentTest extends AnyFlatSpec with Matchers:
  val text: String = "Hello"

  val button = JComponent.button(text, defaultStyler)

  "create JButton" should "with text" in:
    button.text must be(text)

  "change styler" should "change background color" in:
    val newRect   = roundRect(50)
    val newStyler = button.styler.copy(rect = newRect)
    button.modularStyler(newStyler)
    button.styler.rect must be(newRect)
