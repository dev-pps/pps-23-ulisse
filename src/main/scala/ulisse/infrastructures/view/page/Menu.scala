package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.common.ImagePath as ImgPath
import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.components.ui.decorators.Styles
import ulisse.infrastructures.view.utils.ComponentUtils.*

import java.awt.Dimension
import scala.swing.BorderPanel.Position
import scala.swing.{BorderPanel, Component, Orientation}

trait Menu extends ComposedSwing

object Menu:
  given orientationMenu: Orientation.Value = Orientation.Horizontal

  def apply(): Menu = MenuImpl()

  private case class MenuImpl() extends Menu:
    private val widthLabels   = 150
    private val heightLabels  = 50
    private val verticalGap   = 10
    private val mainPadding   = Styles.createPadding(verticalGap, verticalGap)
    private val labelsPadding = Styles.createPadding(5, 5)

    private val mainPanel    = ExtendedSwing.JBorderPanelItem()
    private val iconAppPanel = ExtendedSwing.JFlowPanelItem()
    private val northPanel   = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
    private val southPanel   = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)

    private val mainIcons    = Map(ImgPath.simulation -> "simulation", ImgPath.map -> "map", ImgPath.train -> "train")
    private val controlIcons = Map(ImgPath.settings -> "settings")

    private val iconApp       = ComposedImageLabel.createPictureLabel(ImgPath.logo, "ulisse")
    private val expandButton  = ComposedSwing.JToggleIconButton(ImgPath.expand, ImgPath.compact)
    private val mainLabels    = mainIcons.map(ComposedImageLabel.createIconLabel).toList
    private val controlLabels = controlIcons.map(ComposedImageLabel.createIconLabel).toList

    (mainLabels ++ controlLabels).foreach(_.withDimension(widthLabels, heightLabels))
    mainPanel.rect = mainPanel.rect.withPadding(mainPadding)

    private val addVerticalGap = (panel: ExtendedSwing.JFlowPanelItem) => { panel.vGap = verticalGap; panel }

    iconAppPanel.contents += iconApp.component
    iconAppPanel.contents += expandButton.component

    northPanel.contents += addVerticalGap(iconAppPanel)
    northPanel.contents ++= mainLabels.map(_.centerHorizontally()).map(addVerticalGap)
    southPanel.contents ++= controlLabels.map(_.centerHorizontally()).map(addVerticalGap)

    mainPanel.layout(northPanel) = Position.North
    mainPanel.layout(southPanel) = Position.South

    private def labels: List[ComposedImageLabel] = List(iconApp) ++ mainLabels ++ controlLabels

    private def compact(): Unit = labels.foreach(_.showIcon())

    private def expand(): Unit = labels.foreach(_.showIconAndText())

    override def component[T >: Component]: T = mainPanel
