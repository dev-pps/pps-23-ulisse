package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.common.ImagePath
import ulisse.infrastructures.view.components.ui.composed.{ComposedLabel, ComposedSwing}
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.{Component, Orientation}

trait Dashboard extends ComposedSwing

object Dashboard:
  given orientationMenu: Orientation.Value = Orientation.Vertical

  def apply(): Dashboard = DashboardImpl()

  private case class DashboardImpl() extends Dashboard:
    private val width = 100

    private val newIcon    = ComposedLabel.createIconLabel(ImagePath.addSvgIcon, "new")
    private val iconsPanel = newIcon.centerHorizontally()
    private val mainPanel  = iconsPanel.centerVertically()

    newIcon.withDimension(width, width)

    override def component[T >: Component]: T = mainPanel
