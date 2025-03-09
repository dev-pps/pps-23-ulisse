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

    private val labelsPadding     = Styles.createPadding(5, 0)
    private val verticalGap       = Styles.panelRect.padding.b
    private val widthLabels       = 160
    private val heightLabels      = 50
    private val widthExpandButton = heightLabels / 2

    private val simulationLabel = simulation -> ComposedImageLabel.createIcon(ImgPath.simulationIcon, simulation)
    private val mapLabel        = map        -> ComposedImageLabel.createIcon(ImgPath.mapIcon, map)
    private val trainLabel      = train      -> ComposedImageLabel.createIcon(ImgPath.trainIcon, train)
    private val settingsLabel   = settings   -> ComposedImageLabel.createIcon(ImgPath.settingsIcon, settings)

    private val mainPanel       = BorderPanel().transparent()
    private val mainLabelPanel  = ExtendedSwing.SBorderPanel()
    private val northLabelPanel = ExtendedSwing.SBoxPanel(Orientation.Vertical)
    private val southLabelPanel = ExtendedSwing.SBoxPanel(Orientation.Vertical)

    private val iconApp       = ComposedImageLabel.createTransparentPicture(ImgPath.logo, ulisse)
    private val expandButton  = ComposedSwing.SToggleIconButton(ImgPath.rightCompact, ImgPath.rightCompact)
    private val mainLabels    = Map(simulationLabel, mapLabel, trainLabel)
    private val controlLabels = Map(settingsLabel)

    mainLabelPanel.rect = Styles.panelRect
    iconApp.withFont(Styles.titleDashboardFont)
    expandButton.withDimension(widthExpandButton, widthExpandButton)
    (mainLabels ++ controlLabels).values.foreach(_.withPadding(labelsPadding))
    labels.foreach(_.horizontalAlignment(Alignment.Left))

    northLabelPanel.contents += iconApp.createLeftRight(expandButton)
    northLabelPanel.contents ++= mainLabels.values.flatten(label => List(Swing.VStrut(verticalGap), label.component))
    southLabelPanel.contents ++= controlLabels.values.flatten(label => List(Swing.VStrut(verticalGap), label.component))

    mainLabelPanel.layout(northLabelPanel) = Position.North
    mainLabelPanel.layout(southLabelPanel) = Position.South
    mainPanel.layout(mainLabelPanel) = Position.West

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
