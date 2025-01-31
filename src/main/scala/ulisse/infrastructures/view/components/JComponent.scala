package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.JStyler.*

import java.awt.{BasicStroke, RenderingHints}
import javax.swing.BorderFactory
import scala.swing.{Font as SwingFont, *}

@SuppressWarnings(Array("org.wartremover.warts.Var"))
trait JComponent(var styler: JStyler) extends Component:
  private var currentBackground = styler.background
  opaque = false
  font = styler.swingFont
  listenTo(mouse.moves, mouse.clicks)

  reactions += {
    case event.MouseEntered(_, _, _) => styler.hoverColor.map(color =>
        currentBackground = color
        repaint()
      )
    case event.MouseExited(_, _, _) => styler.hoverColor.map(_ =>
        currentBackground = styler.background
        repaint()
      )
    case event.MousePressed(_, _, _, _, _) => styler.clickColor.map(color =>
        currentBackground = color
        repaint()
      )
    case event.MouseReleased(_, _, _, _, _) => styler.clickColor.map(_ =>
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
    styler = styler.copy(rect = rect)
    repaint()

  def setColorPalette(palette: Palette): Unit =
    styler = styler.copy(palette = palette)
    repaint()

  def setBorder(border: Border): Unit =
    styler = styler.withBorder(border)
    repaint()

object JComponent:
  case class JTextField(colum: Int)(jStyler: JStyler) extends TextField(colum) with JComponent(jStyler):
    peer.setBorder(BorderFactory.createEmptyBorder())
  case class JLabel(label: String)(jStyler: JStyler) extends Label(label) with JComponent(jStyler)
  case class JButton(label: String)(jStyler: JStyler) extends Button(label) with JComponent(jStyler):
    focusPainted = false
    contentAreaFilled = false
    borderPainted = false

  def textField(colum: Int, styler: JStyler): JTextField = JTextField(colum)(styler)
  def label(text: String, styler: JStyler): JLabel       = JLabel(text)(styler)
  def button(text: String, styler: JStyler): JButton     = JButton(text)(styler)

  def modularStylerTextField(colum: Int)(using styler: JStyler) = textField(colum, styler)

  def modularButton(text: String)(using rect: Rect)(using palette: Palette)(using font: Font)(using
      border: Border
  ): JButton =
    button(text, modularStyler)
  def modularRectButton(text: String, palette: Palette, font: Font, border: Border)(using rect: Rect): JButton =
    button(text, modularRectStyler(palette, font, border))
