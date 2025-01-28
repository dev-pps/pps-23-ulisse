package ulisse.infrastructure.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JComponents.JButton

import java.awt.Color

class JButtonTest extends AnyFlatSpec with Matchers:
  val text   = "Hello"
  val button = JButton(text)

  "create JButton" should "with text" in:
    button.button.text must be(text)

//  "change color palette" should "have new color palette" in:
//    button.styleService.colorPalette match
//      case Some(palette) =>
//        val newColorPalette = palette.setBackground(Color.BLACK)
//        val newButton       = button.setColorPalette(newColorPalette)
//        newButton.background must be(Color.BLACK)
//        newColorPalette.hover must be(button.style.colorPalette.hover)
//        newColorPalette.click must be(button.style.colorPalette.click)
//      case None => assert(true)
