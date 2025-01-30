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
  def textField(text: String, styler: JStyler): JTextField = JTextField(text)(styler)
  def label(text: String, styler: JStyler): JLabel         = JLabel(text)(styler)
  def button(text: String, styler: JStyler): JButton       = JButton(text)(styler)

  def puzzleStylesButton(text: String)(using rect: Rect)(using palette: Palette)(using border: Border): JButton =
    button(text, puzzleStyler)
  def puzzleRectButton(text: String, palette: Palette, border: Border)(using rect: Rect): JButton =
    button(text, puzzleRectStyler(palette, border))
  def puzzlePaletteButton(text: String)(using palette: Palette)(using rect: Rect)(using border: Border): JButton =
    button(text, puzzlePaletteStyler(rect, border))
  def puzzleBorderButton(text: String)(using border: Border)(using rect: Rect)(using palette: Palette): JButton =
    button(text, puzzleBorderStyler(rect, palette))
  def puzzleStylerButton(text: String)(using styler: JStyler): JButton = button(text, styler)

  case class JTextField(label: String)(jStyler: JStyler) extends TextField(label) with JComponent(jStyler)
  case class JLabel(label: String)(jStyler: JStyler)     extends Label(label) with JComponent(jStyler)
  case class JButton(label: String)(jStyler: JStyler) extends Button(label) with JComponent(jStyler):
    focusPainted = false
    contentAreaFilled = false
    borderPainted = false
