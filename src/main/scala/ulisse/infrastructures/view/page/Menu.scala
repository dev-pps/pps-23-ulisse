package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.common.ImagePath
import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.composed.{ComposedLabel, ComposedSwing}

import scala.swing.BorderPanel.Position
import scala.swing.{BorderPanel, Component, Orientation}

trait Menu extends ComposedSwing

object Menu:
  given orientationMenu: Orientation.Value = Orientation.Horizontal

  def apply(): Menu = MenuImpl()

  private case class MenuImpl() extends Menu:
    private val mainPanel  = ExtendedSwing.JBorderPanelItem()
    private val northPanel = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
    private val southPanel = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)

    private val iconApp = ComposedLabel.createPictureLabel(ImagePath.logo, "ulisse")
    private val mainIcons =
      List((ImagePath.simulation, "simulation"), (ImagePath.map, "map"), (ImagePath.train, "train")).toMap
    private val controlIcons = List((ImagePath.settings, "settings")).toMap

    private val mainLabels    = mainIcons.map(ComposedLabel.createIconLabel)
    private val controlLabels = controlIcons.map(ComposedLabel.createIconLabel)

    iconApp.withDimension(160, 60)
    mainLabels.foreach(_.withDimension(160, 60))
    controlLabels.foreach(_.withDimension(160, 60))

    northPanel.contents += ExtendedSwing.createFlowPanel(iconApp.component)

    mainLabels.map(icon => ExtendedSwing.createFlowPanel(icon.component))
      .map(panel => { panel.vGap = 10; panel })
      .foreach(northPanel.contents += _)

    controlLabels.map(icon => ExtendedSwing.createFlowPanel(icon.component))
      .map(panel => { panel.vGap = 10; panel })
      .foreach(southPanel.contents += _)

    mainPanel.layout(northPanel) = Position.North
    mainPanel.layout(southPanel) = Position.South

    override def component[T >: Component]: T = mainPanel
