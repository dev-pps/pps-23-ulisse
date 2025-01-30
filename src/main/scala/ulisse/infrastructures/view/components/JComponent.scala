package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.JStyleManager.*

import java.awt.{BasicStroke, RenderingHints}
import scala.swing.*

@SuppressWarnings(Array("org.wartremover.warts.Var"))
trait JComponent(var styleManager: JStyleManager) extends Component:
  private var currentBackground = styleManager.background
  opaque = false
  font = Font("Arial", Font.Bold, 14)
  listenTo(mouse.moves, mouse.clicks)

  reactions += {
    case event.MouseEntered(_, _, _) => styleManager.hover.map(color =>
        currentBackground = color
        repaint()
      )
    case event.MouseExited(_, _, _) => styleManager.hover.map(_ =>
        currentBackground = styleManager.background
        repaint()
      )
    case event.MousePressed(_, _, _, _, _) => styleManager.click.map(color =>
        currentBackground = color
        repaint()
      )
    case event.MouseReleased(_, _, _, _, _) => styleManager.click.map(_ =>
        currentBackground = styleManager.background
        repaint()
      )
  }

  protected override def paintComponent(g: Graphics2D): Unit =
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setColor(currentBackground)
    val arcWidth  = styleManager.arcWidth
    val arcHeight = styleManager.arcHeight

    g.fillRoundRect(0, 0, size.width, size.height, arcWidth, arcHeight)

    styleManager.border.foreach(border =>
      val borderPosition = border.stroke / 2
      val borderSize     = (size.width - border.stroke, size.height - border.stroke)
      g.setColor(border.color)
      g.setStroke(BasicStroke(border.stroke))
      g.drawRoundRect(borderPosition, borderPosition, borderSize._1, borderSize._2, arcWidth, arcHeight)
    )
    super.paintComponent(g)

  def setRect(rect: Rect): Unit =
    styleManager = styleManager.withRect(rect)
    repaint()

  def setColorPalette(palette: Palette): Unit =
    styleManager = styleManager.withPalette(palette)
    repaint()

  def setBorder(border: Border): Unit =
    styleManager = styleManager.withBorder(border)
    repaint()

object JComponent:
  def textField(text: String, manager: JStyleManager): JTextField = JTextField(text, manager)
  def label(text: String, manager: JStyleManager): JLabel         = JLabel(text, manager)
  def button(text: String, manager: JStyleManager): JButton       = JButton(text, manager)

  case class JTextField(label: String, manager: JStyleManager) extends TextField(label) with JComponent(manager)
  case class JLabel(label: String, manager: JStyleManager)     extends Label(label) with JComponent(manager)
  case class JButton(label: String, manager: JStyleManager) extends Button(label) with JComponent(manager):
    focusPainted = false
    contentAreaFilled = false
    borderPainted = false
