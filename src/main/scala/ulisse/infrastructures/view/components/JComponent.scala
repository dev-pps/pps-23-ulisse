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
    case event.MouseExited(_, _, _)         => currentBackground = styleService.colorPalette.background; repaint()
    case event.MouseReleased(_, _, _, _, _) => currentBackground = styleService.colorPalette.background; repaint()
    case event.MousePressed(_, _, _, _, _)  => currentBackground = styleService.colorPalette.click; repaint()
    case event.MouseEntered(_, _, _)        => currentBackground = styleService.colorPalette.hover; repaint()
  }

  protected override def paintComponent(g: Graphics2D): Unit =
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val border         = styleService.border
    val borderPosition = border.width / 2
    val borderSize     = (size.width - border.width, size.height - border.width)

    g.setColor(currentBackground)
    g.setStroke(BasicStroke(border.width))
    g.fillRoundRect(0, 0, size.width, size.height, border.arc, border.arc)
    g.setColor(border.color)
    g.drawRoundRect(borderPosition, borderPosition, borderSize._1, borderSize._2, border.arc, border.arc)
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
