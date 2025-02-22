package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.decorators.Styles.defaultRect

class JItemTest extends AnyFlatSpec with Matchers:
  val text: String = "Hello"

  val button = ExtendedSwing.JButtonItem(text)

  "create JButton" should "with text" in:
    button.text must be(text)

  "change styler" should "change background color" in:
    val newRect = defaultRect.copy(arc = 10)
    button.rect = newRect
    button.rect must be(newRect)
