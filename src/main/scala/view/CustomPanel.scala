package view

import scala.swing.{FlowPanel, Panel}

trait CustomPanel[P <: Panel]

object CustomPanel:

  given transparentPanel: Boolean = false

  val p = CustomPanel(FlowPanel())

  def apply[P <: Panel](panel: P)(using opaque: Boolean): CustomPanel[P] =
    CustomPanelImpl(panel, opaque)

  private case class CustomPanelImpl[P <: Panel](panel: P, opaque: Boolean)
      extends CustomPanel[P]:
    panel.opaque = opaque

//  abstract class Flow(using opaque: Boolean) extends CustomPanel[FlowPanel]
//  abstract class Box(using opaque: Boolean) extends BoxPanel()
//  abstract class Grid(using opaque: Boolean) extends GridPanel()
//  abstract class Border(using opaque: Boolean) extends BorderPanel
//  abstract class GridBag(using opaque: Boolean) extends GridBagPanel
