package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.common.ImagePath
import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.composed.{ComposedImage, ComposedSwing}
import ulisse.infrastructures.view.components.ui.composed.ComposedImage.Direction

import scala.swing.{Component, Orientation, Swing}

trait Dashboard extends ComposedSwing

object Dashboard:
  given directionMenu: Direction = Direction.Vertical

  def apply(): Dashboard = DashboardImpl()

  private case class DashboardImpl() extends Dashboard:

    private val mainPanel = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
    private val panel     = ExtendedSwing.JFlowPanelItem()

    private val label   = "new"
    private val newIcon = ComposedImage.createIconLabel(ImagePath.addSvgIcon, label)

    panel.contents += newIcon.component
    mainPanel.contents += Swing.VGlue
    mainPanel.contents += panel

    override def component[T >: Component]: T = mainPanel
