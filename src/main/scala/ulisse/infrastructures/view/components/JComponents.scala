package ulisse.infrastructures.view.components

import java.awt.{BasicStroke, Color, RenderingHints}
import scala.swing.{event, Button, Font, Graphics2D}

object JComponents:

  object JButton:
    def apply(text: String): JButton = JButton(text)

    case class JButton(text: String):

      @SuppressWarnings(Array("org.wartremover.warts.Var"))
      var button: Button = new Button(text):
        private val strokeWidth       = 2
        private val sizeArc           = 20
        private val borderColor       = Color.decode("#FF4500")
        private val hoverColor        = Color.decode("#FF6347")
        private val clickColor        = Color.decode("#FF4500")
        private val backgroundColor   = Color.decode("#FFA07A")
        private var currentBackground = backgroundColor

        focusPainted = false
        contentAreaFilled = false
        opaque = false
        borderPainted = false
        font = Font("Arial", Font.Bold, 14)

        listenTo(mouse.moves, mouse.clicks)
        reactions += {
          case event.MouseEntered(_, _, _)        => currentBackground = hoverColor; repaint()
          case event.MouseExited(_, _, _)         => currentBackground = backgroundColor; repaint()
          case event.MousePressed(_, _, _, _, _)  => currentBackground = clickColor; repaint()
          case event.MouseReleased(_, _, _, _, _) => currentBackground = backgroundColor; repaint()
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
