package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyler.*

import java.awt.Color

class JItemTest extends AnyFlatSpec with Matchers:
  val text: String = "Hello"

  val button = JItem.button(text, default)

  "create JButton" should "with text" in:
    button.text must be(text)

  "change styler" should "change background color" in:
    val newRect = roundRect(50)
    button.setStyler(button.getStyler.copy(rect = newRect))
    button.getStyler.rect must be(newRect)
