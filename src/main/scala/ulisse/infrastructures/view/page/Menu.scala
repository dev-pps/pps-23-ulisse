package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.common.{ImagePath as ImgPath, Observers}
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.*
import scala.swing.BorderPanel.Position
import scala.swing.event.MouseEvent

/** Represents the menu of the application. */
trait Menu extends ComposedSwing:
  /** Returns true if the menu is expanded. */
  def isExpanded: Boolean

  /** Compact the menu. */
  def compact(): Unit

  /** Expand the menu. */
  def expand(): Unit

object Menu:
  given orientationMenu: Orientation.Value = Orientation.Horizontal

  def apply(parent: Component): Menu = MenuImpl(parent)

  private case class ExpandButtonEvents(menu: Menu) extends Observers.Observer[MouseEvent]:
    override def onClick(data: MouseEvent): Unit = menu.compact()

  private case class UlisseIconEvents(menu: Menu) extends Observers.Observer[MouseEvent]:
    override def onClick(data: MouseEvent): Unit = menu.expand()

  private case class MenuImpl(parent: Component) extends Menu:
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
    private val expandButton  = ComposedSwing.JToggleIconButton(ImgPath.rightCompact, ImgPath.rightCompact)
    private val mainLabels    = mainIcons.map(ComposedImageLabel.createIcon)
    private val controlLabels = controlIcons.map(ComposedImageLabel.createIcon)

    mainPanel.rect = mainPanel.rect.withPadding(mainPadding)
    iconApp.withFont(Styles.titleFont)
    expandButton.withDimension(widthExpandButton, widthExpandButton)
    (mainLabels ++ controlLabels).foreach(_.withPadding(labelsPadding))
    labels.foreach(_.horizontalAlignment(Alignment.Left))

    northPanel.contents += iconApp.createLeftRight(expandButton)
    northPanel.contents ++= mainLabels.flatten(label => List(Swing.VStrut(verticalGap), label.component))
    southPanel.contents ++= controlLabels.flatten(label => List(Swing.VStrut(verticalGap), label.component))

    mainPanel.layout(northPanel) = Position.North
    mainPanel.layout(southPanel) = Position.South

    expand()
    iconApp.attach(UlisseIconEvents(this))
    expandButton.attach(ExpandButtonEvents(this))

    export iconApp.isExpanded

    private def labels: Iterable[ComposedImageLabel] = List(iconApp) ++ mainLabels ++ controlLabels

    override def compact(): Unit =
      labels.foreach(_.showIcon())
      expandButton.component.visible = false

    override def expand(): Unit =
      labels.foreach(_.showIconAndText())
      expandButton.component.visible = true
      labels.foreach(_.withDimension(widthLabels, heightLabels))

    override def component[T >: Component]: T = mainPanel
