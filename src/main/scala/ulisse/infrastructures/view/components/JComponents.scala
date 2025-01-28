package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.JStyle.ColorPalette

import java.awt.{BasicStroke, Color, RenderingHints}
import scala.swing.{event, Button, Font, Graphics2D}

object JComponents:

  object JButton:
    def apply(text: String): JButton = JButton(text)

    case class JButton(text: String):
      val colorPalette: ColorPalette =
        ColorPalette(Color.decode("#FFA07A"), Color.decode("#FF4500"), Color.decode("#FF6347"))

      val button: Button = new Button(text):
        @SuppressWarnings(Array("org.wartremover.warts.Var"))
        private var currentBackground = colorPalette.background

        private val strokeWidth = 2
        private val sizeArc     = 20
        private val borderColor = Color.decode("#FF4500")

        focusPainted = false
        contentAreaFilled = false
        opaque = false
        borderPainted = false
        font = Font("Arial", Font.Bold, 14)

        listenTo(mouse.moves, mouse.clicks)
        reactions += {
          case event.MouseExited(_, _, _)         => currentBackground = colorPalette.background; repaint()
          case event.MouseReleased(_, _, _, _, _) => currentBackground = colorPalette.background; repaint()
          case event.MousePressed(_, _, _, _, _)  => currentBackground = colorPalette.click; repaint()
          case event.MouseEntered(_, _, _)        => currentBackground = colorPalette.hover; repaint()
        }

        override protected def paintComponent(g: Graphics2D): Unit =
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
          g.setStroke(BasicStroke(strokeWidth))
          g.setColor(currentBackground)
          g.fillRoundRect(0, 0, size.width, size.height, sizeArc, sizeArc)
          g.setColor(borderColor)
          val strokeSpace       = strokeWidth / 2
          val sizeWithoutStroke = (size.width - strokeWidth, size.height - strokeWidth)
          g.drawRoundRect(strokeSpace, strokeSpace, sizeWithoutStroke._1, sizeWithoutStroke._2, sizeArc, sizeArc)
          super.paintComponent(g)
