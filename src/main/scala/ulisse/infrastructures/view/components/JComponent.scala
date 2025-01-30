package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.JStyle.JStyleService

import java.awt.{BasicStroke, RenderingHints}
import scala.swing.*

@SuppressWarnings(Array("org.wartremover.warts.Var"))
trait JComponent(var styleService: JStyleService) extends Component:
  private var currentBackground = styleService.colorPalette.background
  opaque = false
  font = Font("Arial", Font.Bold, 14)
  listenTo(mouse.moves, mouse.clicks)

  reactions += {
    case event.MouseEntered(_, _, _) => styleService.colorPalette.hover.map(color =>
        currentBackground = color
        repaint()
      )
    case event.MouseExited(_, _, _) => styleService.colorPalette.hover.map(_ =>
        currentBackground = styleService.colorPalette.background
        repaint()
      )
    case event.MousePressed(_, _, _, _, _) => styleService.colorPalette.click.map(color =>
        currentBackground = color
        repaint()
      )
    case event.MouseReleased(_, _, _, _, _) => styleService.colorPalette.click.map(_ =>
        currentBackground = styleService.colorPalette.background
        repaint()
      )
  }

  protected override def paintComponent(g: Graphics2D): Unit =
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val border         = styleService.border
    val borderPosition = border.stroke / 2
    val borderSize     = (size.width - border.stroke, size.height - border.stroke)

//    g.setColor(currentBackground)
//    g.setStroke(BasicStroke(border.stroke))
//    g.fillRoundRect(0, 0, size.width, size.height, border.arc, border.arc)
//    g.setColor(border.color)
//    g.drawRoundRect(borderPosition, borderPosition, borderSize._1, borderSize._2, border.arc, border.arc)
    super.paintComponent(g)

  def setColorPalette(palette: JStyle.Palette): Unit =
    styleService = styleService.change(palette)
    repaint()

  def setBorder(border: JStyle.Border): Unit =
    styleService = styleService.change(border)
    repaint()

object JComponent:
  def textField(text: String)(using styleService: JStyleService): JTextField = JTextField(text, styleService)
  def label(text: String)(using styleService: JStyleService): JLabel         = JLabel(text, styleService)
  def button(text: String)(using styleService: JStyleService): JButton       = JButton(text, styleService)

  case class JTextField(label: String, service: JStyleService) extends TextField(label) with JComponent(service)
  case class JLabel(label: String, service: JStyleService)     extends Label(label) with JComponent(service)
  case class JButton(label: String, service: JStyleService) extends Button(label) with JComponent(service):
    focusPainted = false
    contentAreaFilled = false
    borderPainted = false
