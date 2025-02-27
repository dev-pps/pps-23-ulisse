package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.common.{ImagePath as ImgPath, Observers}
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.*
import scala.swing.BorderPanel.Position
import scala.swing.event.MouseEvent

/** Represents the dashboard of the application. */
trait Dashboard extends ComposedSwing:
  /** Attaches an observer to the simulation. */
  def attachSimulation(observer: Observers.Observer[MouseEvent]): Unit

  /** Attaches an observer to the map. */
  def attachMap(observer: Observers.Observer[MouseEvent]): Unit

  /** Attaches an observer to the train. */
  def attachTrain(observer: Observers.Observer[MouseEvent]): Unit

  /** Attaches an observer to the settings. */
  def attachSettings(observer: Observers.Observer[MouseEvent]): Unit

  /** Returns true if the dashboard is expanded. */
  def isExpanded: Boolean

  /** Compact the dashboard. */
  def compact(): Unit

  /** Expand the dashboard. */
  def expand(): Unit

object Dashboard:
  private given orientationDashboard: Orientation.Value = Orientation.Horizontal

  /** Creates a new instance of the dashboard. */
  def apply(): Dashboard = DashboardImpl()

  private case class ExpandButtonEvents(dashboard: Dashboard) extends Observers.Observer[MouseEvent]:
    override def onClick(data: MouseEvent): Unit = dashboard.compact()

  private case class UlisseIconEvents(dashboard: Dashboard) extends Observers.Observer[MouseEvent]:
    override def onClick(data: MouseEvent): Unit = dashboard.expand()

  private case class DashboardImpl() extends Dashboard:
    private val ulisse     = "Ulisse"
    private val simulation = "simulation"
    private val map        = "map"
    private val train      = "train"
    private val settings   = "settings"

    private val mainPadding       = Styles.createPadding(15, 15)
    private val labelsPadding     = Styles.createPadding(5, 0)
    private val verticalGap       = mainPadding.b
    private val widthLabels       = 160
    private val heightLabels      = 50
    private val widthExpandButton = heightLabels / 2

    private val simulationLabel = simulation -> ComposedImageLabel.createIcon(ImgPath.simulation, simulation)
    private val mapLabel        = map        -> ComposedImageLabel.createIcon(ImgPath.map, map)
    private val trainLabel      = train      -> ComposedImageLabel.createIcon(ImgPath.train, train)
    private val settingsLabel   = settings   -> ComposedImageLabel.createIcon(ImgPath.settings, settings)

    private val mainPanel  = ExtendedSwing.JBorderPanelItem()
    private val northPanel = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
    private val southPanel = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)

    private val iconApp       = ComposedImageLabel.createTransparentPicture(ImgPath.logo, ulisse)
    private val expandButton  = ComposedSwing.JToggleIconButton(ImgPath.rightCompact, ImgPath.rightCompact)
    private val mainLabels    = Map(simulationLabel, mapLabel, trainLabel)
    private val controlLabels = Map(settingsLabel)

    mainPanel.rect = mainPanel.rect.withPadding(mainPadding)
    iconApp.withFont(Styles.titleFont)
    expandButton.withDimension(widthExpandButton, widthExpandButton)
    (mainLabels ++ controlLabels).values.foreach(_.withPadding(labelsPadding))
    labels.foreach(_.horizontalAlignment(Alignment.Left))

    northPanel.contents += iconApp.createLeftRight(expandButton)
    northPanel.contents ++= mainLabels.values.flatten(label => List(Swing.VStrut(verticalGap), label.component))
    southPanel.contents ++= controlLabels.values.flatten(label => List(Swing.VStrut(verticalGap), label.component))

    mainPanel.layout(northPanel) = Position.North
    mainPanel.layout(southPanel) = Position.South

    expand()
    iconApp.attach(UlisseIconEvents(this))
    expandButton.attach(ExpandButtonEvents(this))

    export iconApp.isExpanded, simulationLabel._2.attach as attachSimulation, mapLabel._2.attach as attachMap,
      trainLabel._2.attach as attachTrain, settingsLabel._2.attach as attachSettings

    private def labels: Iterable[ComposedImageLabel] = List(iconApp) ++ (mainLabels ++ controlLabels).values

    override def compact(): Unit =
      labels.foreach(_.showIcon())
      expandButton.component.visible = false

    override def expand(): Unit =
      labels.foreach(_.showIconAndText())
      expandButton.component.visible = true
      labels.foreach(_.withDimension(widthLabels, heightLabels))

    override def component[T >: Component]: T = mainPanel
