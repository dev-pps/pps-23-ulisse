package ulisse.infrastructures.view.components.ui

import ulisse.infrastructures.view.components.ui.decorators.ImageEffects.{PictureEffect, SVGEffect}
import ulisse.infrastructures.view.components.ui.decorators.SwingEnhancements.{FontEffect, ShapeEffect}

import java.awt.{BorderLayout, FlowLayout}
import javax.swing.{BoxLayout, JLayeredPane}
import scala.swing.*

object ExtendedSwing:

  case class LayeredPanel() extends BorderPanel with ShapeEffect:
    private val layeredPane = new JLayeredPane()
    layeredPane.setLayout(new BorderLayout())
    layout(Component.wrap(layeredPane)) = BorderPanel.Position.Center

    def add(component: Component): Unit =
      layeredPane.add(component.peer)
      revalidate()
      repaint()

  case class JBorderPanelItem() extends BorderPanel with ShapeEffect

  def createFlowPanel(component: Component*): FlowPanel =
    val panel = JFlowPanelItem()
    panel.contents ++= component
    panel

  case class JFlowPanelItem() extends FlowPanel with ShapeEffect with FontEffect:
    private val layout = new FlowLayout(FlowLayout.CENTER, 0, 0)
    peer.setLayout(layout)
    export layout._

  case class JBoxPanelItem(orientation: Orientation.Value) extends BoxPanel(orientation) with ShapeEffect

  case class JPanelItem() extends Panel with ShapeEffect

  def createPicturePanel(path: String): PicturePanel =
    val panel = PicturePanel()
    panel.picture = path
    panel

  def createSVGPanel(path: String): SVGPanel =
    val panel = SVGPanel()
    panel.svgIcon = path
    panel

  case class PicturePanel() extends Panel with PictureEffect
  case class SVGPanel()     extends Panel with SVGEffect

  case class JButtonItem(label: String) extends Button(label) with ShapeEffect with FontEffect

  case class JLabelItem(label: String) extends Label(label) with ShapeEffect with FontEffect

  case class JTextFieldItem(colum: Int) extends TextField(colum) with ShapeEffect with FontEffect

//  private final case class DrawnPanel(iconDrawer: (UIElement, Graphics2D) => Unit) extends ImagePanel:
//    override def paintComponent(g: Graphics2D): Unit =
//      super.paintComponent(g)
//      iconDrawer(this, g)
//
//  object Example:
//    def drawCross(preferredLength: Int, preferredThickness: Int, color: Color)(
//      element: UIElement,
//      g: Graphics2D
//    ): Unit =
//      val oldColor = g.getColor
//      g.setColor(color)
//      val center = (element.size.width / 2, element.size.height / 2)
//      val length = math.min(math.min(element.size.width / 2, element.size.height / 2), preferredLength)
//      val thickness = math.min(math.min(element.size.width / 2, element.size.height / 2), preferredThickness)
//      g.fillRect(center._1 - thickness / 2, center._2 - length / 2, thickness, length)
//      g.fillRect(center._1 - length / 2, center._2 - thickness / 2, length, thickness)
//      g.setColor(oldColor)
//
//    val imagePanelExample: ImagePanel = ImagePanel.createImagePanel("icons/logo.jpg")
//    val svgPanelExample: ImagePanel = ImagePanel.createSVGPanel("icons/map.svg", Color.RED)
//    val drawnPanelExample: ImagePanel = ImagePanel.createDrawnPanel(drawCross(20, 2, Color.BLUE))
