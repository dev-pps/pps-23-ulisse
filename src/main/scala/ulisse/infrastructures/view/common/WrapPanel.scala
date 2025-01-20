package ulisse.infrastructures.view.common

import scala.swing.*

trait WrapPanel[+P <: Panel]:
  def panel(): P
  def components(): Seq[Component]
  def componentsOf[T <: Component](implicit ct: reflect.ClassTag[T]): Seq[T] = components().collect({ case c: T => c })
  def setVisible(visible: Boolean): Unit
  def visible: Boolean

object WrapPanel:
  def apply[P <: Panel](panel: P)(components: Component*)(using opaque: Boolean): WrapPanel[P] =
    WrapPanelImpl(panel)(components: _*)

  def flow(components: Component*)(using opaque: Boolean): WrapPanel[FlowPanel] =
    WrapPanel(FlowPanel())(components: _*)

  def box(orientation: Orientation.Value)(components: Component*)(using opaque: Boolean): WrapPanel[BoxPanel] =
    WrapPanel(BoxPanel(orientation))(components: _*)

  def border(components: Component*)(using opaque: Boolean): WrapPanel[BorderPanel] =
    WrapPanel(BorderPanel())(components: _*)

  private case class WrapPanelImpl[+P <: Panel](mainPanel: P)(items: Component*)(using opaque: Boolean)
      extends WrapPanel[P]:
    items.map(_.peer).foreach(mainPanel.peer.add)
    mainPanel.opaque = opaque

    override def panel(): P                         = mainPanel
    override def components(): Seq[Component]       = items
    override def setVisible(visible: Boolean): Unit = panel().visible = visible
    override def visible: Boolean                   = panel().visible

//  abstract class Flow(using opaque: Boolean) extends CustomPanel[FlowPanel]
//  abstract class Box(using opaque: Boolean) extends BoxPanel()
//  abstract class Grid(using opaque: Boolean) extends GridPanel()
//  abstract class Border(using opaque: Boolean) extends BorderPanel
//  abstract class GridBag(using opaque: Boolean) extends GridBagPanel
