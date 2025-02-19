package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.SwingEnhancements.*

import java.awt.FlowLayout
import scala.swing.*

object ExtendedSwing:

  case class JBorderPanelItem() extends BorderPanel
      with EnhancedLook with ShapeEffect with ColorEffect with FontEffect with BorderEffect

  case class JFlowPanelItem() extends FlowPanel
      with EnhancedLook with ShapeEffect with ColorEffect with FontEffect with BorderEffect:
    private val layout = new FlowLayout(FlowLayout.CENTER, 0, 0)
    peer.setLayout(layout)
    export layout._

  case class JBoxPanelItem(orientation: Orientation.Value) extends BoxPanel(orientation)
      with EnhancedLook with ShapeEffect with ColorEffect with FontEffect with BorderEffect

  case class JPanelItem() extends Panel
      with EnhancedLook with ShapeEffect with ColorEffect with FontEffect with BorderEffect

  case class JButtonItem(label: String) extends Button(label)
      with EnhancedLook with ShapeEffect with ColorEffect with FontEffect with BorderEffect

  case class JLabelItem(label: String) extends Label(label)
      with EnhancedLook with ShapeEffect with ColorEffect with FontEffect with BorderEffect

  case class JTextFieldItem(colum: Int) extends TextField(colum)
      with EnhancedLook with ShapeEffect with ColorEffect with FontEffect with BorderEffect
