package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.common.ImagePath
import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.composed.{ComposedLabel, ComposedSwing}

import scala.swing.{Component, Orientation, Swing}

trait Dashboard extends ComposedSwing

object Dashboard:
  given orientationMenu: Orientation.Value = Orientation.Vertical

  def apply(): Dashboard = DashboardImpl()

  private case class DashboardImpl() extends Dashboard:
    private val newIcon   = ComposedLabel.createIconLabel(ImagePath.addSvgIcon, "new")
    private val mainPanel = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
    private val panel     = ExtendedSwing.JFlowPanelItem()

    newIcon.withDimension(100, 100)

    panel.contents += newIcon.component
    mainPanel.contents += Swing.VGlue
    mainPanel.contents += panel

    override def component[T >: Component]: T = mainPanel
