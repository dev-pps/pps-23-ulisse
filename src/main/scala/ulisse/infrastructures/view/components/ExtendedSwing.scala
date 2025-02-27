package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.decorators.ImageEffects.{PictureEffect, SVGEffect}
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.{EnhancedLook, FontEffect, ShapeEffect}
import ulisse.infrastructures.view.components.styles.Styles

import java.awt.FlowLayout
import javax.swing.JLayeredPane
import scala.swing.*

object ExtendedSwing:

  case class LayeredPanel private (private val layeredPane: JLayeredPane) extends BorderPanel with EnhancedLook:
    def this() = this(JLayeredPane())
    layout(Component.wrap(layeredPane)) = BorderPanel.Position.Center

    def add(component: Component): Unit =
      layeredPane.add(component.peer)
      revalidate()

    override def revalidate(): Unit =
      layeredPane.getComponents.foreach(_.setBounds(0, 0, layeredPane.getWidth, layeredPane.getHeight))
      super.revalidate()

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

  case class SVGPanel() extends Panel with SVGEffect

  case class JButtonItem(label: String) extends Button(label) with ShapeEffect with FontEffect

  case class JLabelItem(label: String) extends Label(label) with ShapeEffect with FontEffect

  case class JTextFieldItem(colum: Int) extends TextField(colum) with ShapeEffect with FontEffect
