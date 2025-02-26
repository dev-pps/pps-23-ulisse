package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.common.ImagePath as ImgPath
import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.components.ui.decorators.Styles
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.BorderPanel.Position
import scala.swing.{BorderPanel, Component, Orientation, Swing}

trait Menu extends ComposedSwing

object Menu:
  given orientationMenu: Orientation.Value = Orientation.Horizontal

  def apply(): Menu = MenuImpl()

  private case class MenuImpl() extends Menu:
    private val mainIcons     = Map(ImgPath.simulation -> "simulation", ImgPath.map -> "map", ImgPath.train -> "train")
    private val controlIcons  = Map(ImgPath.settings -> "settings")
    private val verticalGap   = 15
    private val mainPadding   = Styles.createPadding(verticalGap, verticalGap)
    private val labelsPadding = Styles.createPadding(5, 0)
    private val widthLabels   = 180
    private val heightLabels  = 50

    private val mainPanel    = ExtendedSwing.JBorderPanelItem()
    private val iconAppPanel = ExtendedSwing.JBorderPanelItem()
    private val northPanel   = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
    private val southPanel   = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)

    private val iconApp       = ComposedImageLabel.createTransparentPicture(ImgPath.logo, "Ulisse")
    private val expandButton  = ComposedSwing.JToggleIconButton(ImgPath.rightCompact, ImgPath.rightExpand)
    private val mainLabels    = mainIcons.map(ComposedImageLabel.createIcon)
    private val controlLabels = controlIcons.map(ComposedImageLabel.createIcon)

    iconApp.withFont(Styles.titleFont)
    mainPanel.rect = mainPanel.rect.withPadding(mainPadding)
    (mainLabels ++ controlLabels).foreach(_.withPadding(labelsPadding))
    (mainLabels ++ controlLabels).foreach(_.withDimension(widthLabels, heightLabels))

    iconAppPanel.layout(iconApp.component) = Position.West
    iconAppPanel.layout(expandButton.component) = Position.East

    northPanel.contents += iconAppPanel
    northPanel.contents ++= mainLabels.flatten(label => List(Swing.VStrut(verticalGap), label.component))
    southPanel.contents ++= controlLabels.flatten(label => List(Swing.VStrut(verticalGap), label.component))

    mainPanel.layout(northPanel) = Position.North
    mainPanel.layout(southPanel) = Position.South

    private def labels: List[ComposedImageLabel] = List(iconApp) ++ mainLabels ++ controlLabels

    private def compact(): Unit = { labels.foreach(_.showIcon()); expandButton.component.visible = false }

    private def expand(): Unit = { labels.foreach(_.showIconAndText()); expandButton.component.visible = true }

    override def component[T >: Component]: T = mainPanel
