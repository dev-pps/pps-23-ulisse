package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.JStyler._

import java.awt.{BasicStroke, RenderingHints}
import javax.swing.BorderFactory
import scala.swing.*

@SuppressWarnings(Array("org.wartremover.warts.Var"))
trait JComponent(var styler: JStyler) extends Component:
  private var currentBackground = styler.background
  opaque = false
  font = Font("Lucida Grande", Font.Plain, 13)
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
  def puzzleStylerTextField(colum: Int)(using styler: JStyler) = textField(colum, styler)
  def textField(colum: Int, styler: JStyler): JTextField       = JTextField(colum)(styler)
  def label(text: String, styler: JStyler): JLabel             = JLabel(text)(styler)
  def button(text: String, styler: JStyler): JButton           = JButton(text)(styler)

  def puzzleStylesButton(text: String)(using rect: Rect)(using palette: Palette)(using font: JFont)(using
      border: Border
  ): JButton =
    button(text, puzzleStyler)
  def puzzleRectButton(text: String, palette: Palette, font: JFont, border: Border)(using rect: Rect): JButton =
    button(text, puzzleRectStyler(palette, font, border))

  case class JTextField(colum: Int)(jStyler: JStyler) extends TextField(colum) with JComponent(jStyler):
    peer.setBorder(BorderFactory.createEmptyBorder())
  case class JLabel(label: String)(jStyler: JStyler) extends Label(label) with JComponent(jStyler)
  case class JButton(label: String)(jStyler: JStyler) extends Button(label) with JComponent(jStyler):
    focusPainted = false
    contentAreaFilled = false
    borderPainted = false
