package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.common.ImagePath as ImgPath
import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.BorderPanel.Position
import scala.swing.{BorderPanel, Component, Orientation}

trait Menu extends ComposedSwing

object Menu:
  given orientationMenu: Orientation.Value = Orientation.Horizontal

  def apply(): Menu = MenuImpl()

  private case class MenuImpl() extends Menu:
    private val verticalGap = 10
    private val widthLabel  = 160
    private val heightLabel = 60

    private val mainPanel  = ExtendedSwing.JBorderPanelItem()
    private val northPanel = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
    private val southPanel = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)

    private val iconApp      = ComposedImageLabel.createPictureLabel(ImgPath.logo, "ulisse")
    private val mainIcons    = Map(ImgPath.simulation -> "simulation", ImgPath.map -> "map", ImgPath.train -> "train")
    private val controlIcons = Map(ImgPath.settings -> "settings")

    private val mainLabels    = mainIcons.map(ComposedImageLabel.createIconLabel)
    private val controlLabels = controlIcons.map(ComposedImageLabel.createIconLabel)

    iconApp.withDimension(widthLabel, heightLabel)
    mainLabels.foreach(_.withDimension(widthLabel, heightLabel))
    controlLabels.foreach(_.withDimension(widthLabel, heightLabel))

    private val addVerticalGap = (panel: ExtendedSwing.JFlowPanelItem) => { panel.vGap = verticalGap; panel }

    northPanel.contents += iconApp.centerHorizontally()
    northPanel.contents ++= mainLabels.map(_.centerHorizontally()).map(addVerticalGap)
    southPanel.contents ++= controlLabels.map(_.centerHorizontally()).map(addVerticalGap)

    mainPanel.layout(northPanel) = Position.North
    mainPanel.layout(southPanel) = Position.South

    override def component[T >: Component]: T = mainPanel
