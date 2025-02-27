package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.decorators.ImageEffects.{PictureEffect, SVGEffect}
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.{EnhancedLook, FontEffect, ShapeEffect}

import java.awt.FlowLayout
import javax.swing.JLayeredPane
import scala.swing.*

object ExtendedSwing:

  case class SLayeredPanel private (private val layeredPane: JLayeredPane) extends BorderPanel with EnhancedLook:
    def this() = this(JLayeredPane())
    layout(Component.wrap(layeredPane)) = BorderPanel.Position.Center

    def add(component: Component): Unit =
      layeredPane.add(component.peer)
      revalidate()

    override def revalidate(): Unit =
      layeredPane.getComponents.foreach(_.setBounds(0, 0, layeredPane.getWidth, layeredPane.getHeight))
      super.revalidate()

  case class SBorderPanel() extends BorderPanel with ShapeEffect

  def createFlowPanel(component: Component*): FlowPanel =
    val panel = SFlowPanel()
    panel.contents ++= component
    panel

  case class SFlowPanel() extends FlowPanel with ShapeEffect with FontEffect:
    private val layout = new FlowLayout(FlowLayout.CENTER, 0, 0)
    peer.setLayout(layout)
    export layout._

  case class SBoxPanel(orientation: Orientation.Value) extends BoxPanel(orientation) with ShapeEffect

  case class SPanel() extends Panel with ShapeEffect

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

  case class SButton(label: String) extends Button(label) with ShapeEffect with FontEffect:
    focusPainted = false
    contentAreaFilled = false

  case class SLabel(label: String) extends Label(label) with ShapeEffect with FontEffect

  case class STextField(colum: Int) extends TextField(colum) with ShapeEffect with FontEffect
