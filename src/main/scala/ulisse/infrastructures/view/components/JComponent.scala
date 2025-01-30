package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.JStyler.*

import java.awt.{BasicStroke, RenderingHints}
import scala.swing.*

@SuppressWarnings(Array("org.wartremover.warts.Var"))
trait JComponent(var styler: JStyler) extends Component:
  private var currentBackground = styler.background
  opaque = false
  font = Font("Arial", Font.Bold, 14)
  listenTo(mouse.moves, mouse.clicks)

  reactions += {
    case event.MouseEntered(_, _, _) => styler.hover.map(color =>
        currentBackground = color
        repaint()
      )
    case event.MouseExited(_, _, _) => styler.hover.map(_ =>
        currentBackground = styler.background
        repaint()
      )
    case event.MousePressed(_, _, _, _, _) => styler.click.map(color =>
        currentBackground = color
        repaint()
      )
    case event.MouseReleased(_, _, _, _, _) => styler.click.map(_ =>
        currentBackground = styler.background
        repaint()
      )
  }

  protected override def paintComponent(g: Graphics2D): Unit =
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setColor(currentBackground)
    val arcWidth  = styler.arcWidth
    val arcHeight = styler.arcHeight

    g.fillRoundRect(0, 0, size.width, size.height, arcWidth, arcHeight)

    styler.border.foreach(border =>
      val borderPosition = border.stroke / 2
      val borderSize     = (size.width - border.stroke, size.height - border.stroke)
      g.setColor(border.color)
      g.setStroke(BasicStroke(border.stroke))
      g.drawRoundRect(borderPosition, borderPosition, borderSize._1, borderSize._2, arcWidth, arcHeight)
    )
    super.paintComponent(g)

  def setRect(rect: Rect): Unit =
    styler = styler.withRect(rect)
    repaint()

  def setColorPalette(palette: Palette): Unit =
    styler = styler.withPalette(palette)
    repaint()

  def setBorder(border: Border): Unit =
    styler = styler.withBorder(border)
    repaint()

object JComponent:
  def textField(text: String, manager: JStyler): JTextField = JTextField(text, manager)
  def label(text: String, manager: JStyler): JLabel         = JLabel(text, manager)
  def button(text: String, manager: JStyler): JButton       = JButton(text, manager)

  case class JTextField(label: String, jStyler: JStyler) extends TextField(label) with JComponent(jStyler)
  case class JLabel(label: String, jStyler: JStyler)     extends Label(label) with JComponent(jStyler)
  case class JButton(label: String, jStyler: JStyler) extends Button(label) with JComponent(jStyler):
    focusPainted = false
    contentAreaFilled = false
    borderPainted = false
