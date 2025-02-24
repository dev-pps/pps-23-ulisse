package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.common.ImagePath
import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.composed.ComposedImage.Direction
import ulisse.infrastructures.view.components.ui.composed.{ComposedImage, ComposedSwing}

import scala.swing.BorderPanel.Position
import scala.swing.{BorderPanel, Component, Orientation}

trait Menu extends ComposedSwing

object Menu:
  given directionMenu: Direction = Direction.Horizontal

  def apply(): Menu = MenuImpl()

  private case class MenuImpl() extends Menu:

    private val mainPanel              = ExtendedSwing.JBorderPanelItem()
    private val boxDashboardPanelNorth = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
    private val boxDashboardPanelSouth = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)

    private val iconApp      = ComposedImage.createPictureLabel(ImagePath.logo, "ulisse")
    private val mainIcons    = List(ImagePath.simulation, ImagePath.map, ImagePath.train)
    private val controlIcons = List(ImagePath.settings)

    private val mainLabels    = mainIcons.map(icon => ComposedImage.createIconLabel(icon, icon))
    private val controlLabels = controlIcons.map(icon => ComposedImage.createIconLabel(icon, icon))

    boxDashboardPanelNorth.contents += ExtendedSwing.createFlowPanel(iconApp.component)

    mainLabels.map(icon => ExtendedSwing.createFlowPanel(icon.component))
      .map(panel => { panel.vGap = 10; panel })
      .foreach(boxDashboardPanelNorth.contents += _)

    controlLabels.map(icon => ExtendedSwing.createFlowPanel(icon.component))
      .map(panel => { panel.vGap = 10; panel })
      .foreach(boxDashboardPanelSouth.contents += _)

    mainPanel.layout(boxDashboardPanelNorth) = Position.North
    mainPanel.layout(boxDashboardPanelSouth) = Position.South

    override def component[T >: Component]: T = mainPanel
