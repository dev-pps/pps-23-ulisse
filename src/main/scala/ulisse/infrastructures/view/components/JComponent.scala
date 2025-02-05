package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.JStyler.*

import java.awt.{BasicStroke, FlowLayout, RenderingHints}
import javax.swing.BorderFactory
import scala.swing.{Font as SwingFont, *}

@SuppressWarnings(Array("org.wartremover.warts.Var"))
trait JComponent(private var styler: JStyler) extends Component:
  private var currentBackground = styler.background
  listenTo(mouse.moves, mouse.clicks)
  opaque = false
  initStyler()

  private def initStyler(): Unit =
    currentBackground = styler.background
    styler.size.foreach(size => preferredSize = new Dimension(size.width, size.height))
    styler.border.foreach(style =>
      border = BorderFactory.createEmptyBorder(style.stroke, style.stroke, style.stroke, style.stroke)
    )
    font = styler.swingFont

    revalidate()
    repaint()

  def modularStyler(using newStyler: JStyler): Unit =
    styler = newStyler
    initStyler()

  def getStyler: JStyler = styler

  def setStyler(newStyler: JStyler): Unit =
    styler = newStyler
    initStyler()

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

  override protected def paintBorder(g: Graphics2D): Unit =
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    styler.border.foreach(border =>
      val borderPosition = border.stroke / 2
      val borderSize     = (size.width - border.stroke, size.height - border.stroke)
      g.setColor(border.color)
      g.setStroke(BasicStroke(border.stroke))
      g.drawRoundRect(borderPosition, borderPosition, borderSize._1, borderSize._2, styler.arcWidth, styler.arcHeight)
    )
    super.paintBorder(g)

  protected override def paintComponent(g: Graphics2D): Unit =
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setColor(currentBackground)
    g.fillRoundRect(0, 0, size.width, size.height, styler.arcWidth, styler.arcHeight)
    super.paintComponent(g)

object JComponent:

  case class JFlowPanel()(jStyler: JStyler) extends FlowPanel with JComponent(jStyler):
    private val layout = new FlowLayout(FlowLayout.CENTER, 0, 0)
    peer.setLayout(layout)
    opaque = false

  case class JBoxPanel(orientation: Orientation.Value)(jStyler: JStyler) extends BoxPanel(orientation)
      with JComponent(jStyler):
    opaque = false

  case class JPanel()(jStyler: JStyler) extends Panel with JComponent(jStyler):
    peer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))

  case class JButton(label: String)(jStyler: JStyler) extends Button(label) with JComponent(jStyler):
    focusPainted = false
    borderPainted = false
    contentAreaFilled = false

  case class JLabel(label: String)(jStyler: JStyler) extends Label(label) with JComponent(jStyler)

  case class JTextField(colum: Int)(jStyler: JStyler) extends TextField(colum) with JComponent(jStyler):
    peer.setBorder(BorderFactory.createEmptyBorder())

  def createFlowPanel(styler: JStyler): JFlowPanel                               = JFlowPanel()(styler)
  def createBoxPanel(orientation: Orientation.Value, styler: JStyler): JBoxPanel = JBoxPanel(orientation)(styler)

  def button(text: String, styler: JStyler): JButton     = JButton(text)(styler)
  def label(text: String, styler: JStyler): JLabel       = JLabel(text)(styler)
  def textField(colum: Int, styler: JStyler): JTextField = JTextField(colum)(styler)

  def modularButton(text: String)(using styler: JStyler): JButton     = button(text, styler)
  def modularLabel(text: String)(using styler: JStyler): JLabel       = label(text, styler)
  def modularTextField(colum: Int)(using styler: JStyler): JTextField = textField(colum, styler)
