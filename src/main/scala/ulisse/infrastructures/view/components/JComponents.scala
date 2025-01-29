package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.JStyle.{Border, ColorPalette, StyleService}

import java.awt.{BasicStroke, Color, RenderingHints}
import scala.swing.*
import scala.swing.MenuBar.NoMenuBar.{listenTo, mouse, reactions}

object JComponents:

  trait JComponent extends Component
//    val styleService: StyleService
//    def setColorPalette(colorPalette: ColorPalette): JComponent[T]

  object JComponent:
    def apply(style: JStyle*): JComponent = JComponentImpl(StyleService(style: _*))

    private case class JComponentImpl(styleService: StyleService) extends JComponent:
      @SuppressWarnings(Array("org.wartremover.warts.Var"))
      private var currentBackground =
        styleService.colorPalette.getOrElse(ColorPalette(Color.WHITE, Color.WHITE, Color.WHITE)).background
      opaque = false
      font = Font("Arial", Font.Bold, 14)

      listenTo(mouse.moves, mouse.clicks)

      styleService.colorPalette.map(palette =>
        reactions += {
          case event.MouseExited(_, _, _)         => currentBackground = palette.background; repaint()
          case event.MouseReleased(_, _, _, _, _) => currentBackground = palette.background; repaint()
          case event.MousePressed(_, _, _, _, _)  => currentBackground = palette.click; repaint()
          case event.MouseEntered(_, _, _)        => currentBackground = palette.hover; repaint()
        }
      )

      override protected def paintComponent(g: Graphics2D): Unit =
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        for {
          colorPalette <- styleService.colorPalette
          border       <- styleService.border
        } yield {
          g.setStroke(BasicStroke(border.width))
          g.setColor(currentBackground)
          g.fillRoundRect(0, 0, size.width, size.height, border.arc, border.arc)
          g.setColor(border.color)
          val borderPosition = border.width / 2
          val borderSize     = (size.width - border.width, size.height - border.width)
          g.drawRoundRect(borderPosition, borderPosition, borderSize._1, borderSize._2, border.arc, border.arc)
        }

        super.paintComponent(g)

  trait JButton:
    val button: Button
    val styleService: StyleService
    def setColorPalette(colorPalette: ColorPalette): JButton

  object JButton:
    def apply(text: String): JButton = JButtonImpl(
      text,
      StyleService(
        ColorPalette(Color.decode("#FFA07A"), Color.decode("#FF4500"), Color.decode("#FF6347")),
        Border(Color.decode("#FF4500"), 2, 20)
      )
    )

    private case class JButtonImpl(text: String, styleService: StyleService) extends JButton:
      override def setColorPalette(colorPalette: ColorPalette): JButton =
        copy(styleService = styleService.change(colorPalette))

      val button: Button = new Button(text):
        @SuppressWarnings(Array("org.wartremover.warts.Var"))
        private var currentBackground =
          styleService.colorPalette.getOrElse(ColorPalette(Color.WHITE, Color.WHITE, Color.WHITE)).background
        focusPainted = false
        contentAreaFilled = false
        opaque = false
        borderPainted = false
        font = Font("Arial", Font.Bold, 14)

        listenTo(mouse.moves, mouse.clicks)

        styleService.colorPalette.map(palette =>
          reactions += {
            case event.MouseExited(_, _, _)         => currentBackground = palette.background; repaint()
            case event.MouseReleased(_, _, _, _, _) => currentBackground = palette.background; repaint()
            case event.MousePressed(_, _, _, _, _)  => currentBackground = palette.click; repaint()
            case event.MouseEntered(_, _, _)        => currentBackground = palette.hover; repaint()
          }
        )

        override protected def paintComponent(g: Graphics2D): Unit =
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
          for {
            colorPalette <- styleService.colorPalette
            border       <- styleService.border
          } yield {
            g.setStroke(BasicStroke(border.width))
            g.setColor(currentBackground)
            g.fillRoundRect(0, 0, size.width, size.height, border.arc, border.arc)
            g.setColor(border.color)
            val borderPosition = border.width / 2
            val borderSize     = (size.width - border.width, size.height - border.width)
            g.drawRoundRect(borderPosition, borderPosition, borderSize._1, borderSize._2, border.arc, border.arc)
          }

          super.paintComponent(g)
