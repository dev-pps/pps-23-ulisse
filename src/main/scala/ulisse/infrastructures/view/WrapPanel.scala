package ulisse.infrastructures.view

import scala.swing.*

trait WrapPanel[+P <: Panel]:
  def panel: P
  def setVisible(visible: Boolean): Unit
  def addComponent(component: Component): Unit
  def visible: Boolean

object WrapPanel:

  def apply[P <: Panel](panel: P)(using opaque: Boolean): WrapPanel[P] =
    WrapPanelImpl(panel, opaque)

  def flow(using opaque: Boolean): WrapPanel[FlowPanel] = WrapPanel(FlowPanel())

  def box(orientation: Orientation.Value)(using
      opaque: Boolean
  ): WrapPanel[BoxPanel] =
    WrapPanel(BoxPanel(orientation))

  def border(using opaque: Boolean): WrapPanel[BorderPanel] =
    WrapPanel(BorderPanel())

  private case class WrapPanelImpl[+P <: Panel](panel: P, opaque: Boolean)
      extends WrapPanel[P]:
    panel.opaque = opaque

    override def setVisible(visible: Boolean): Unit = panel.visible = visible

    override def addComponent(component: Component): Unit =
      panel.peer.add(component.peer)

    override def visible: Boolean = panel.visible

//  private case class Wrap[+P <: Panel](panel: P):
//    export panel.{contents, visible}

//  abstract class Flow(using opaque: Boolean) extends CustomPanel[FlowPanel]
//  abstract class Box(using opaque: Boolean) extends BoxPanel()
//  abstract class Grid(using opaque: Boolean) extends GridPanel()
//  abstract class Border(using opaque: Boolean) extends BorderPanel
//  abstract class GridBag(using opaque: Boolean) extends GridBagPanel
