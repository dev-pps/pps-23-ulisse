package ulisse.infrastructures.view.components.ui

import ulisse.infrastructures.view.components.ui.decorators.SwingEnhancements.{BorderEffect, FontEffect, ShapeEffect}

import java.awt.FlowLayout
import scala.swing.*

object ExtendedSwing:
  trait ExtendedSwingStyle extends ShapeEffect with FontEffect with BorderEffect

  case class JBorderPanelItem() extends BorderPanel with ExtendedSwingStyle

  case class JFlowPanelItem() extends FlowPanel with ExtendedSwingStyle:
    private val layout = new FlowLayout(FlowLayout.CENTER, 0, 0)
    peer.setLayout(layout)
    export layout._

  case class JBoxPanelItem(orientation: Orientation.Value) extends BoxPanel(orientation) with ExtendedSwingStyle

  case class JPanelItem() extends Panel with ExtendedSwingStyle

  case class JButtonItem(label: String) extends Button(label) with ExtendedSwingStyle

  case class JLabelItem(label: String) extends Label(label) with ExtendedSwingStyle

  case class JTextFieldItem(colum: Int) extends TextField(colum) with ExtendedSwingStyle
