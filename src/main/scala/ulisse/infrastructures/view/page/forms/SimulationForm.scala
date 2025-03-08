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
import ulisse.entities.station.StationEnvironmentElement
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.{EnhancedLook, ShapeEffect}
import ulisse.infrastructures.view.map.MapElement
import ulisse.infrastructures.view.map.MapSimulation.TrainMapElement
import ulisse.utils.Times.*

import scala.swing.BorderPanel.Position
import scala.swing.{Component, Orientation, Swing}

/** Represents the simulation form of the application. */
trait SimulationForm extends Form:

  /** Attach the start simulation observer to the form of type [[SimulationInfo]]. */
  def attachStartSimulation(observer: ClickObserver[SimulationInfo]): Unit

  /** Attach the reset simulation observer to the form of type [[Unit]]. */
  def attachResetSimulation(observer: ClickObserver[Unit]): Unit

  /** Sets the simulation information. */
  def setEngineConfiguration(engine: EngineConfiguration): Unit

  /** Prints the simulation information. */
  def showSimulationData(info: SimulationData): Unit

  /** Shows the station simulation. */
  def showStationSimulation(station: StationEnvironmentElement): Unit

  /** Shows the route simulation. */
  def showRouteSimulation(route: RouteEnvironmentElement): Unit

  /** Shows the train simulation. */
  def showTrainSimulation(train: TrainMapElement): Unit

/** Companion object of the [[SimulationForm]]. */
object SimulationForm:
  /** Creates a new instance of simulation form. */
  def apply(): SimulationForm = SimulationFormImpl()

  /** Represents the simulation form data. */
  final case class SimulationInfo(step: String, cyclePerSecond: String):
    def stepInt: Option[Int]           = step.toIntOption
    def cyclePerSecondInt: Option[Int] = cyclePerSecond.toIntOption

  /** Represents the start simulation event. */
  final case class PlaySimulationEvent(adapter: SimulationPageAdapter) extends ClickObserver[SimulationInfo]:
    override def onClick(info: SimulationInfo): Unit =
      info.stepInt.fold(println("error"))(step =>
        adapter.setupEngine(step, info.cyclePerSecondInt)
        adapter.start()
      )

  /** Represents the reset simulation event. */
  final case class ResetSimulationEvent(adapter: SimulationPageAdapter) extends ClickObserver[Unit]:
    override def onClick(info: Unit): Unit = adapter.reset()

  /** Represents the take station event. */
  final case class TakeStationEvent(form: SimulationForm, infoSimulation: SimulationInfoAdapter)
      extends ClickObserver[MapElement[StationEnvironmentElement]]:
    override def onClick(data: MapElement[StationEnvironmentElement]): Unit = form.showStationSimulation(data.element)

  /** Represents the take route event. */
  final case class TakeRouteEvent(form: SimulationForm, infoSimulation: SimulationInfoAdapter)
      extends ClickObserver[MapElement[RouteEnvironmentElement]]:
    override def onClick(data: MapElement[RouteEnvironmentElement]): Unit = form.showRouteSimulation(data.element)

  final case class TakeTrainEvent(form: SimulationForm, infoSimulation: SimulationInfoAdapter)
      extends ClickObserver[MapElement[TrainMapElement]]:
    override def onClick(data: MapElement[TrainMapElement]): Unit = form.showTrainSimulation(data.element)

  private case class SimulationFormImpl() extends SimulationForm:
    private val mainPanel: SBorderPanel                     = SBorderPanel()
    private val stepSize: ComposedSwing.InfoTextField       = ComposedSwing createInfoTextField "Step"
    private val cyclePerSecond: ComposedSwing.InfoTextField = ComposedSwing createInfoTextField "Cycle"
    private val playButton  = ExtendedSwing createFormButtonWith ("Play", Styles.formButtonRect)
    private val resetButton = ExtendedSwing createFormButtonWith ("Reset", Styles.formButtonRect)
    private val form        = BaseForm("Simulation", stepSize, cyclePerSecond)
    private val space       = 10

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

    override def showStationSimulation(statio: StationEnvironmentElement): Unit =
      val infoStr = s"""STATION SIMULATION:
             \nSTATION NAME: 
             \nSTATION COORDINATES: 
             \nSTATION TRACKS: """
      elementInfoArea.setText(infoStr)

    override def showRouteSimulation(route: RouteEnvironmentElement): Unit =
      val infoStr = s"""ROUTE SIMULATION:
                 \nROUTE NAME: 
                 \nROUTE DEPARTURE: 
                 \nROUTE ARRIVAL: 
                 \nROUTE DISTANCE: 
                 \nROUTE DURATION: """
      elementInfoArea.setText(infoStr)

    override def showTrainSimulation(train: TrainMapElement): Unit =
      val infoStr = s"""TRAIN SIMULATION:
                     \nTRAIN NAME: 
                     \nTRAIN POSITION: 
                     \nTRAIN SPEED: 
                     \nTRAIN DIRECTION: """
      elementInfoArea.setText(infoStr)

    override def component[T >: Component]: T = mainPanel
