package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.JStyler.*
import ulisse.infrastructures.view.components.SwingEnhancements.*

import java.awt.{BasicStroke, FlowLayout, RenderingHints}
import scala.swing.{Font as SwingFont, *}

object ExtendedSwing:

  case class JBorderPanelItem() extends BorderPanel
      with Enhanced with RectEffect with ColorEffect with FontEffect with BorderEffect

  case class JFlowPanelItem() extends FlowPanel
      with Enhanced with RectEffect with ColorEffect with FontEffect with BorderEffect:
    private val layout = new FlowLayout(FlowLayout.CENTER, 0, 0)
    peer.setLayout(layout)
    export layout._

  case class JBoxPanelItem(orientation: Orientation.Value) extends BoxPanel(orientation)
      with Enhanced with RectEffect with ColorEffect with FontEffect with BorderEffect

  case class JPanelItem() extends Panel
      with Enhanced with RectEffect with ColorEffect with FontEffect with BorderEffect

  case class JButtonItem(label: String) extends Button(label)
      with Enhanced with RectEffect with ColorEffect with FontEffect with BorderEffect

  case class JLabelItem(label: String) extends Label(label)
      with Enhanced with RectEffect with ColorEffect with FontEffect with BorderEffect

  case class JTextFieldItem(colum: Int) extends TextField(colum)
      with Enhanced with RectEffect with ColorEffect with FontEffect with BorderEffect
