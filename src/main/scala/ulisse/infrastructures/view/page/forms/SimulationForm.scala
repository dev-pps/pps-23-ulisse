package ulisse.infrastructures.view.page.forms

import ulisse.adapters.input.{SimulationInfoAdapter, SimulationPageAdapter}
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.simulation.data.{EngineConfiguration, SimulationData}
import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.ExtendedSwing.SBorderPanel
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.page.forms.Form.BaseForm
import ulisse.infrastructures.view.page.forms.SimulationForm.SimulationInfo
import ulisse.entities.simulation.data.Statistics.*
import ulisse.infrastructures.view.utils.ComponentUtils.*
import ulisse.infrastructures.view.utils.Swings.given_ExecutionContext
import ulisse.entities.station.StationEnvironmentElement
import ulisse.entities.station.StationEnvironmentElement.StationEnvironmentInfo
import ulisse.entities.train.TrainAgents
import ulisse.entities.train.TrainAgents.TrainAgentInfo
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.{EnhancedLook, ShapeEffect}
import ulisse.infrastructures.view.map.MapElement
import ulisse.infrastructures.view.map.MapSimulation.TrainMapElement
import ulisse.infrastructures.view.page.workspaces.SimulationWorkspace
import ulisse.utils.Times.*

import java.awt.geom.Point2D
import scala.swing.BorderPanel.Position
import scala.swing.{Component, Orientation, Swing}

/** Represents the simulation form of the application. */
trait SimulationForm extends Form:

  /** Sets the button to play. */
  def setButtonPlay(): Unit

  /** Sets the button to pause. */
  def setButtonPause(): Unit

  /** Attach the start simulation observer to the form of type [[SimulationInfo]]. */
  def attachStartSimulation(observer: ClickObserver[SimulationInfo]): Unit

  /** Attach the reset simulation observer to the form of type [[Unit]]. */
  def attachResetSimulation(observer: ClickObserver[Unit]): Unit

  /** Sets the simulation information. */
  def setEngineConfiguration(engine: EngineConfiguration): Unit

  /** Prints the simulation information. */
  def showSimulationData(info: SimulationData): Unit

  /** Shows the station simulation. */
  def showStationSimulation(station: StationEnvironmentInfo): Unit

  /** Shows the route simulation. */
  def showRouteSimulation(route: RouteEnvironmentElement): Unit

  /** Shows the train simulation. */
  def showTrainSimulation(train: TrainAgentInfo, position: Point2D.Double): Unit

/** Companion object of the [[SimulationForm]]. */
object SimulationForm:
  /** Creates a new instance of simulation form. */
  def apply(): SimulationForm = SimulationFormImpl()

  /** Represents the simulation form data. */
  final case class SimulationInfo(step: String, cyclePerSecond: String):
    def stepInt: Option[Int]           = step.toIntOption
    def cyclePerSecondInt: Option[Int] = cyclePerSecond.toIntOption

  /** Represents the start simulation event. */
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  final case class PlaySimulationEvent(
      adapter: SimulationPageAdapter,
      workspace: SimulationWorkspace,
      form: SimulationForm
  ) extends ClickObserver[SimulationInfo]:
    private var isPlaying = false

    override def onClick(info: SimulationInfo): Unit =
      if !isPlaying then
        info.stepInt.fold(form.showError("error"))(step =>
          adapter.setupEngine(step, info.cyclePerSecondInt)
          adapter.start()
          form.setButtonPause()
        )
      else
        adapter.stop()
        form.setButtonPlay()
      isPlaying = !isPlaying

  /** Represents the reset simulation event. */
  final case class ResetSimulationEvent(adapter: SimulationPageAdapter, workspace: SimulationWorkspace)
      extends ClickObserver[Unit]:
    override def onClick(info: Unit): Unit =
      adapter.reset()
      workspace.initSimulation()

  /** Represents the take station event. */
  final case class TakeStationEvent(form: SimulationForm, infoSimulation: SimulationInfoAdapter)
      extends ClickObserver[MapElement[StationEnvironmentElement]]:
    override def onClick(data: MapElement[StationEnvironmentElement]): Unit =
      infoSimulation.stationInfo(data.element).onComplete(_.fold(
        println,
        station => station.foreach(form.showStationSimulation)
      ))

  /** Represents the take route event. */
  final case class TakeRouteEvent(form: SimulationForm, infoSimulation: SimulationInfoAdapter)
      extends ClickObserver[MapElement[RouteEnvironmentElement]]:
    override def onClick(data: MapElement[RouteEnvironmentElement]): Unit =
      form.showRouteSimulation(data.element)

  final case class TakeTrainEvent(form: SimulationForm, infoSimulation: SimulationInfoAdapter)
      extends ClickObserver[MapElement[TrainMapElement]]:
    override def onClick(data: MapElement[TrainMapElement]): Unit =
      infoSimulation.trainInfo(data.element.train).onComplete(_.fold(
        println,
        train => train.foreach(form.showTrainSimulation(_, data.element.position))
      ))

  private case class SimulationFormImpl() extends SimulationForm:
    private val mainPanel: SBorderPanel                     = SBorderPanel()
    private val stepSize: ComposedSwing.InfoTextField       = ComposedSwing createInfoTextField "Step"
    private val cyclePerSecond: ComposedSwing.InfoTextField = ComposedSwing createInfoTextField "Cycle"
    private val playButton  = ExtendedSwing createFormButtonWith ("Play", Styles.formButtonRect)
    private val resetButton = ExtendedSwing createFormButtonWith ("Reset", Styles.formButtonRect)
    private val form        = BaseForm("Simulation", stepSize, cyclePerSecond)

    private val infoArea        = ExtendedSwing.STextArea()
    private val elementInfoArea = ExtendedSwing.STextArea()
    private val infoPanel       = ExtendedSwing.SBoxPanel(Orientation.Vertical)
    private val flowPanel       = ExtendedSwing.SFlowPanel().transparent()
    infoPanel.contents += Swing.VGlue
    infoPanel.contents += Component.wrap(infoArea)
    infoPanel.contents += Swing.VGlue
    infoPanel.contents += Component.wrap(elementInfoArea)
    flowPanel.hGap = space
    flowPanel.vGap = space + 5
    flowPanel.contents += infoPanel

    buttonPanel.contents += playButton
    buttonPanel.contents += resetButton

    mainPanel.layout(flowPanel) = Position.North
    mainPanel.layout(form.component) = Position.South

    private val playObservable  = Observers.createObservable[SimulationInfo]
    private val resetObservable = Observers.createObservable[Unit]

    playButton attachClick (playObservable toObserver (_ => SimulationInfo(stepSize.text, cyclePerSecond.text)))
    resetButton attachClick (resetObservable toObserver (_ => ()))

    export form.{component => _, _}

    private def setButtonPlayText(text: String): Unit =
      playButton.text = text
      playButton.repaint()

    override def setButtonPlay(): Unit = setButtonPlayText("Play")

    override def setButtonPause(): Unit = setButtonPlayText("Pause")

    override def attachStartSimulation(observer: ClickObserver[SimulationInfo]): Unit =
      playObservable attachClick observer

    override def attachResetSimulation(observer: ClickObserver[Unit]): Unit =
      resetObservable attachClick observer

    override def setEngineConfiguration(engine: EngineConfiguration): Unit =
      stepSize.text = s"${engine.stepSize}"
      cyclePerSecond.text = s"${engine.cyclesPerSecond.getOrElse(0)}"

    override def showSimulationData(info: SimulationData): Unit =
      val infoStr = s"""SIMULATION TIME: ${info.millisecondsElapsed.toTime}
         \nENVIRONMENT TIME: ${info.simulationEnvironment.time}
         \nCUMULATIVE DELAY: ${info.simulationEnvironment.cumulativeDelay}
         \nAVERAGE DELAY: ${info.simulationEnvironment.averageDelay}
         \nTRAIN IN STATIONS: ${info.simulationEnvironment.percTrainsInStations} %
         \nTRAIN ON ROUTE: ${info.simulationEnvironment.percTrainsOnRoutes} %
         \nSTATION LOAD: ${info.simulationEnvironment.percStationsLoad} %"""
      infoArea.setText(infoStr)

    override def showStationSimulation(station: StationEnvironmentInfo): Unit =
      val StationEnvironmentInfo(env, cumulativeDelay, averageDelay) = station

      val infoStr = s"""STATION [${env.name}]:
             \nTrains: ${env.trains.size} / ${env.numberOfPlatforms}
             \nCumulative Delay: $cumulativeDelay
             \nAverage Delay: $averageDelay """
      elementInfoArea.setText(infoStr)

    override def showRouteSimulation(route: RouteEnvironmentElement): Unit =
      val infoStr = s"""ROUTE:
                 \nStations: ${route.departure.name} - ${route.arrival.name}
                 \nTrains: ${route.trains.size}
                 \nType: ${route.typology}"""
      elementInfoArea.setText(infoStr)

    override def showTrainSimulation(trainInfo: TrainAgentInfo, position: Point2D.Double): Unit =
      val infoStr = s"""TRAIN [${trainInfo.train.name}]:
                     \nName: ${trainInfo.delayInCurrentTimetable.getOrElse("No Timetable")}
                     \nPosition: ${position.x} - ${position.y}
                     \nDistance Travelled: ${trainInfo.train.distanceTravelled}
                     \nSpeed: ${trainInfo.train.state.motionData.speed}"""
      elementInfoArea.setText(infoStr)

    override def component[T >: Component]: T = mainPanel
