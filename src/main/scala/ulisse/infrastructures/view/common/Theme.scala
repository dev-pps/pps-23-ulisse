package ulisse.infrastructures.view.common

import java.awt.Color

trait Theme(
    val background: Color,
    val element: Color,
    val hover: Color,
    val forwardClick: Color,
    val backwardClick: Color,
    val trueClick: Color,
    val falseClick: Color
)

object Theme:
  val light: Theme = Light()

  private case class Light() extends Theme(
        Color.decode("#f7f8ff"),
        Color.decode("#d6dbf5"),
        Color.decode("#a2aace"),
        Color.decode("#418dff"),
        Color.decode("#c0c1c8"),
        Color.decode("#59f759"),
        Color.decode("#f75959")
      )
