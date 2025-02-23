package ulisse.infrastructures.view.common

import java.awt.Color

object Themes:
  extension (color: Color)
    def withAlpha(alpha: Int): Color =
      new Color(color.getRed, color.getGreen, color.getBlue, alpha)

  trait Theme(
      val background: Color,
      val text: Color,
      val element: Color,
      val overlay: Color,
      val click: Color,
      val backClick: Color,
      val trueClick: Color,
      val falseClick: Color
  )

  object Theme:
    val light: Theme = Light()

    private case class Light() extends Theme(
          background = Color.decode("#f7f8ff"),
          text = Color.decode("#2C2C2C"),
          element = Color.decode("#d6dbf5"),
          overlay = Color.decode("#a2aace"),
          click = Color.decode("#418dff"),
          backClick = Color.decode("#c0c1c8"),
          trueClick = Color.decode("#59f759"),
          falseClick = Color.decode("#f75959")
        )
