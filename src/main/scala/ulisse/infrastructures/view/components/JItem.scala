package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.JStyler.*

import java.awt.{BasicStroke, FlowLayout, RenderingHints}
import javax.swing.BorderFactory
import scala.swing.{Font as SwingFont, *}

@SuppressWarnings(Array("org.wartremover.warts.Var"))
trait JItem(private var styler: JStyler) extends Component:
  private var currentColor = styler.background
  listenTo(mouse.moves, mouse.clicks)
  opaque = false
  initStyler()

  private def initStyler(): Unit =
    currentColor = styler.background
    val width  = styler.size.width.getOrElse(size.width)
    val height = styler.size.height.getOrElse(size.height)
    size.setSize(width, height)
    border = BorderFactory.createEmptyBorder(
      styler.padding.height,
      styler.padding.width,
      styler.padding.height,
      styler.padding.width
    )

    font = styler.swingFont
    foreground = styler.colorFont

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
        currentColor = color
        repaint()
      )
    case event.MouseExited(_, _, _) => styler.hoverColor.map(_ =>
        currentColor = styler.background
        repaint()
      )
    case event.MousePressed(_, _, _, _, _) => styler.clickColor.map(color =>
        currentColor = color
        repaint()
      )
    case event.MouseReleased(_, _, _, _, _) => styler.clickColor.map(_ =>
        currentColor = styler.background
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
      g.drawRoundRect(borderPosition, borderPosition, borderSize._1, borderSize._2, styler.arc, styler.arc)
    )
    super.paintBorder(g)

  protected override def paintComponent(g: Graphics2D): Unit =
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setColor(currentColor)
    g.fillRoundRect(0, 0, size.width, size.height, styler.arc, styler.arc)
    super.paintComponent(g)

object JItem:

  case class JBorderPanelItem()(jStyler: JStyler) extends BorderPanel with JItem(jStyler):
    opaque = false

  case class JFlowPanelItem()(jStyler: JStyler) extends FlowPanel with JItem(jStyler):
    private val layout = new FlowLayout(FlowLayout.CENTER, 0, 0)
    peer.setLayout(layout)
    opaque = false
    export layout._

  case class JBoxPanelItem(orientation: Orientation.Value)(jStyler: JStyler) extends BoxPanel(orientation)
      with JItem(jStyler):
    opaque = false

  case class JPanelItem()(jStyler: JStyler) extends Panel with JItem(jStyler)

  case class JButtonItem(label: String)(jStyler: JStyler) extends Button(label) with JItem(jStyler):
    focusPainted = false
    borderPainted = false
    contentAreaFilled = false

  case class JLabelItem(label: String)(jStyler: JStyler)  extends Label(label) with JItem(jStyler)
  case class JTextFieldItem(colum: Int)(jStyler: JStyler) extends TextField(colum) with JItem(jStyler)

  def createBorderPanel(styler: JStyler): JBorderPanelItem = JBorderPanelItem()(styler)
  def createFlowPanel(styler: JStyler): JFlowPanelItem     = JFlowPanelItem()(styler)
  def createBoxPanel(orientation: Orientation.Value, styler: JStyler): JBoxPanelItem =
    JBoxPanelItem(orientation)(styler)

  def button(text: String, styler: JStyler): JButtonItem     = JButtonItem(text)(styler)
  def label(text: String, styler: JStyler): JLabelItem       = JLabelItem(text)(styler)
  def textField(colum: Int, styler: JStyler): JTextFieldItem = JTextFieldItem(colum)(styler)

  def modularButton(text: String)(using styler: JStyler): JButtonItem     = button(text, styler)
  def modularLabel(text: String)(using styler: JStyler): JLabelItem       = label(text, styler)
  def modularTextField(colum: Int)(using styler: JStyler): JTextFieldItem = textField(colum, styler)
