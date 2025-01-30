package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyle.JStyleService

import java.awt.Color

class JComponents extends AnyFlatSpec with Matchers:
  given serviceStyle: JStyleService = JStyle.empty()

  val text   = "Hello"
  val button = JComponent.button(text)

  "create JButton" should "with text" in:
    button.text must be(text)

//  "change color palette" should "have new color palette" in:
//    val newPalette = JStyle.Palette(Color.BLACK, Color.RED, Color.GREEN)
//    button.setColorPalette(newPalette)
//    button.styleService.colorPalette must be(newPalette)

  "change border" should "have new border" in:
    val newBorder = JStyle.border(Color.BLACK, 2, 20)
    button.setBorder(newBorder)
    button.styleService.border must be(newBorder)
