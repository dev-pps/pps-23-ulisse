package view

import scala.swing.Panel

trait WrapPanel[P <: Panel]:
  def panel: P

object WrapPanel:

  def apply[P <: Panel](panel: P)(using opaque: Boolean): WrapPanel[P] =
    WrapPanelImpl(panel, opaque)

  given transparentPanel: Boolean = false

  private case class WrapPanelImpl[P <: Panel](panel: P, opaque: Boolean)
      extends WrapPanel[P]:
    panel.opaque = opaque

//  abstract class Flow(using opaque: Boolean) extends CustomPanel[FlowPanel]
//  abstract class Box(using opaque: Boolean) extends BoxPanel()
//  abstract class Grid(using opaque: Boolean) extends GridPanel()
//  abstract class Border(using opaque: Boolean) extends BorderPanel
//  abstract class GridBag(using opaque: Boolean) extends GridBagPanel
