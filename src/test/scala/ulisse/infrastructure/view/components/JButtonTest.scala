package ulisse.infrastructure.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JComponents.JButton
import ulisse.infrastructures.view.components.JStyle

import java.awt.Color

class JButtonTest extends AnyFlatSpec with Matchers:
  val text   = "Hello"
  val button = JButton(text)

  "create JButton" should "with text" in:
    button.button.text must be(text)

  "change color palette" should "have new color palette" in:
    val newPalette = JStyle.ColorPalette(Color.BLACK, Color.RED, Color.GREEN)
    button.setColorPalette(newPalette)

    button.styleService.colorPalette match
      case Some(palette) => palette must be(newPalette)
      case None          => fail("Palette not found")
