package ulisse.infrastructure.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JComponents.JButton

class JButtonTest extends AnyFlatSpec with Matchers:
  val text   = "Hello"
  val button = JButton(text)

  "create JButton" should "with text" in:
    button.button.text must be(text)

//  "create JButton" should "with default values" in:
//    button.sizeArc must be(20)
//    button.strokeWidth must be(2)
//    button.borderColor must be(java.awt.Color.decode("#FF4500"))
//    button.hoverColor must be(java.awt.Color.decode("#FF6347"))
//    button.clickColor must be(java.awt.Color.decode("#FF4500"))
//    button.backgroundColor must be(java.awt.Color.decode("#FFA07A"))
//    button.currentBackground must be(button.backgroundColor)
