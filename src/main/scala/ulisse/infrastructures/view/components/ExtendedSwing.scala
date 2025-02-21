package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.SwingEnhancements.*

import java.awt.FlowLayout
import scala.swing.*

object ExtendedSwing:

  case class JBorderPanelItem() extends BorderPanel with ShapeEffect with FontEffect

  case class JFlowPanelItem() extends FlowPanel with ShapeEffect with FontEffect:
    private val layout = new FlowLayout(FlowLayout.CENTER, 0, 0)
    peer.setLayout(layout)
    export layout._

  case class JBoxPanelItem(orientation: Orientation.Value) extends BoxPanel(orientation)
      with ShapeEffect with FontEffect with BorderEffect

  case class JPanelItem() extends Panel with ShapeEffect with FontEffect

  case class JButtonItem(label: String) extends Button(label) with ShapeEffect with FontEffect

  case class JLabelItem(label: String) extends Label(label) with ShapeEffect with FontEffect

  case class JTextFieldItem(colum: Int) extends TextField(colum) with ShapeEffect with FontEffect
