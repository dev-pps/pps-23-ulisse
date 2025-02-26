package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.common.{ImagePath as ImgPath, Observers}
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.*
import scala.swing.BorderPanel.Position
import scala.swing.event.MouseEvent

trait Menu extends ComposedSwing

object Menu:
  given orientationMenu: Orientation.Value = Orientation.Horizontal

  def apply(): Menu = MenuImpl()

  private case class MenuImpl() extends Menu with Observers.Observer[MouseEvent]:
    private val mainIcons     = Map(ImgPath.simulation -> "simulation", ImgPath.map -> "map", ImgPath.train -> "train")
    private val controlIcons  = Map(ImgPath.settings -> "settings")
    private val mainPadding   = Styles.createPadding(15, 15)
    private val labelsPadding = Styles.createPadding(5, 0)
    private val verticalGap   = mainPadding.b
    private val widthLabels   = 160
    private val heightLabels  = 50
    private val widthExpandButton = heightLabels / 2

    private val mainPanel  = ExtendedSwing.JBorderPanelItem()
    private val northPanel = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
    private val southPanel = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)

    private val iconApp       = ComposedImageLabel.createTransparentPicture(ImgPath.logo, "Ulisse")
    private val expandButton  = ComposedSwing.JToggleIconButton(ImgPath.rightCompact, ImgPath.rightExpand)
    private val mainLabels    = mainIcons.map(ComposedImageLabel.createIcon)
    private val controlLabels = controlIcons.map(ComposedImageLabel.createIcon)

    mainPanel.rect = mainPanel.rect.withPadding(mainPadding)
    iconApp.withFont(Styles.titleFont)
    expandButton.withDimension(widthExpandButton, widthExpandButton)
    (mainLabels ++ controlLabels).foreach(_.withPadding(labelsPadding))
    labels.foreach(_.withDimension(widthLabels, heightLabels))
    labels.foreach(_.horizontalAlignment(Alignment.Left))

    northPanel.contents += iconApp.createLeftRight(expandButton)
    northPanel.contents ++= mainLabels.flatten(label => List(Swing.VStrut(verticalGap), label.component))
    southPanel.contents ++= controlLabels.flatten(label => List(Swing.VStrut(verticalGap), label.component))

    iconApp.attach(this)

    mainPanel.layout(northPanel) = Position.North
    mainPanel.layout(southPanel) = Position.South

    private def labels: Iterable[ComposedImageLabel] = List(iconApp) ++ mainLabels ++ controlLabels

    private def compact(): Unit = { labels.foreach(_.showIcon()); expandButton.component.visible = false }

    private def expand(): Unit = { labels.foreach(_.showIconAndText()); expandButton.component.visible = true }

    override def component[T >: Component]: T = mainPanel

    override def onClick(data: MouseEvent): Unit = println("CIAOOOO")
