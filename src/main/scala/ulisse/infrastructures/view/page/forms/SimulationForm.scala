package ulisse.infrastructures.view.page.forms

import ulisse.adapters.input.{SimulationInfoAdapter, SimulationPageAdapter}
import ulisse.applications.ports.SimulationPorts.Input.Preset
import ulisse.applications.ports.SimulationPorts.Input.Preset.Simple
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.simulation.data.Statistics.*
import ulisse.entities.simulation.data.{EngineConfiguration, SimulationData}
import ulisse.entities.station.StationEnvironmentElement
import ulisse.entities.station.StationEnvironmentElement.StationEnvironmentInfo
import ulisse.entities.train.TrainAgents
import ulisse.entities.train.TrainAgents.TrainAgentInfo
import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.ExtendedSwing.SBorderPanel
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.map.MapElement
import ulisse.infrastructures.view.map.MapSimulation.TrainMapElement
import ulisse.infrastructures.view.page.forms.Form.BaseForm
import ulisse.infrastructures.view.page.forms.SimulationForm.SimulationInfo
import ulisse.infrastructures.view.page.workspaces.SimulationWorkspace
import ulisse.infrastructures.view.utils.ComponentUtils.*
import ulisse.infrastructures.view.utils.Swings.given_ExecutionContext
import ulisse.utils.Times.*

import java.awt.geom.Point2D
import scala.swing.BorderPanel.Position
import scala.swing.{Component, Orientation, Swing}

/** Represents the simulation form of the application. */
trait SimulationForm extends Form:

  /** Sets the button to play. */
  def play(): Unit

  /** Sets the button to pause. */
  def reset(): Unit

  /** Checks if the simulation is playing. */
  def isPlaying: Boolean

  /** Attach the start simulation observer to the form of type [[SimulationInfo]]. */
  def attachStartSimulation(observer: ClickObserver[SimulationInfo]): Unit

  /** Attach the reset simulation observer to the form of type [[Unit]]. */
  def attachResetSimulation(observer: ClickObserver[Unit]): Unit

  /** Attach the simple simulation observer to the form of type [[Unit]]. */
  def attachSimpleSimulation(observer: ClickObserver[Unit]): Unit

  /** Attach the middle simulation observer to the form of type [[Unit]]. */
  def attachNormalSimulation(observer: ClickObserver[Unit]): Unit

  /** Attach the complex simulation observer to the form of type [[Unit]]. */
  def attachComplexSimulation(observer: ClickObserver[Unit]): Unit

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
    override def onClick(info: SimulationInfo): Unit =
      if !form.isPlaying then
        info.stepInt.fold(form.showError("error"))(step =>
          adapter.setupEngine(step, info.cyclePerSecondInt)
          adapter.start()
          form.play()
        )
      else adapter.stop()

  /** Represents the reset simulation event. */
  final case class ResetSimulationEvent(
      adapter: SimulationPageAdapter,
      workspace: SimulationWorkspace,
      form: SimulationForm
  ) extends ClickObserver[Unit]:
    override def onClick(info: Unit): Unit =
      adapter.reset()
      form.reset()
      workspace.initSimulation()

  /** Represents the setup simulation event. */
  final case class SetupSimulationEvent(
      adapter: SimulationPageAdapter,
      workspace: SimulationWorkspace,
      form: SimulationForm,
      preset: Preset
  ) extends ClickObserver[Unit]:
    override def onClick(info: Unit): Unit =
      adapter.initSimulationUsing(preset)
      form.reset()
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
      form.resetError()
      form.showRouteSimulation(data.element)

  /** Represents the take train event. */
  final case class TakeTrainEvent(form: SimulationForm, infoSimulation: SimulationInfoAdapter)
      extends ClickObserver[MapElement[TrainMapElement]]:
    override def onClick(data: MapElement[TrainMapElement]): Unit =
      form.resetError()
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

    private val settersPanel = ExtendedSwing.SFlowPanel().transparent()
    private val simpleSim    = ExtendedSwing createFormButtonWith ("Simple", Styles.formButtonRect)
    private val normalSim    = ExtendedSwing createFormButtonWith ("Normal", Styles.formButtonRect)
    private val complexSim   = ExtendedSwing createFormButtonWith ("Complex", Styles.formButtonRect)

    settersPanel.hGap = space
    settersPanel.contents += simpleSim
    settersPanel.contents += normalSim
    settersPanel.contents += complexSim

    private val southPanel = ExtendedSwing.SBoxPanel(Orientation.Vertical)
    southPanel.contents += form.component
    southPanel.contents += settersPanel
    southPanel.contents += Swing.VStrut(space)

    private val infoArea        = ExtendedSwing.STextArea()
    private val elementInfoArea = ExtendedSwing.STextArea()
    private val infoPanel       = ExtendedSwing.SBoxPanel(Orientation.Vertical)
    private val flowPanel       = ExtendedSwing.SFlowPanel().transparent()
    infoPanel.contents += Swing.VStrut(space)
    infoPanel.contents += Component.wrap(infoArea)
    infoPanel.contents += Swing.VStrut(space + 5)
    infoPanel.contents += Component.wrap(elementInfoArea)
    flowPanel.hGap = space
    flowPanel.vGap = space + 5
    flowPanel.contents += infoPanel

    buttonPanel.contents += playButton
    buttonPanel.contents += resetButton

    mainPanel.layout(flowPanel) = Position.North
    mainPanel.layout(southPanel) = Position.South

    private val playObservable    = Observers.createObservable[SimulationInfo]
    private val resetObservable   = Observers.createObservable[Unit]
    private val simpleObservable  = Observers.createObservable[Unit]
    private val normalObservable  = Observers.createObservable[Unit]
    private val complexObservable = Observers.createObservable[Unit]

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _isPlaying = false

    playButton attachClick (playObservable toObserver (_ => SimulationInfo(stepSize.text, cyclePerSecond.text)))
    resetButton attachClick (resetObservable toObserver (_ => ()))
    simpleSim attachClick (simpleObservable toObserver (_ => ()))
    normalSim attachClick (normalObservable toObserver (_ => ()))
    complexSim attachClick (complexObservable toObserver (_ => ()))

    export form.{component => _, _}

    private def setButtonPlayText(text: String): Unit =
      playButton.text = text
      playButton.repaint()

    override def play(): Unit =
      if isPlaying then
        setButtonPlayText("Play")
      else
        setButtonPlayText("Pause")
      isPlaying = !isPlaying

    override def reset(): Unit =
      setButtonPlayText("Play")
      isPlaying = false

    override def isPlaying: Boolean               = _isPlaying
    private def isPlaying_=(value: Boolean): Unit = _isPlaying = value

    override def attachStartSimulation(observer: ClickObserver[SimulationInfo]): Unit =
      playObservable attachClick observer

    override def attachResetSimulation(observer: ClickObserver[Unit]): Unit =
      resetObservable attachClick observer

    override def attachSimpleSimulation(observer: ClickObserver[Unit]): Unit =
      simpleObservable attachClick observer

    override def attachNormalSimulation(observer: ClickObserver[Unit]): Unit =
      normalObservable attachClick observer

    override def attachComplexSimulation(observer: ClickObserver[Unit]): Unit =
      complexObservable attachClick observer

    override def setEngineConfiguration(engine: EngineConfiguration): Unit =
      stepSize.text = s"${engine.stepSize}"
      cyclePerSecond.text = s"${engine.cyclesPerSecond.getOrElse(0)}"

    override def showSimulationData(info: SimulationData): Unit =
      val simuTime       = s"Simulation time: ${info.millisecondsElapsed.toTime}"
      val envTime        = s"Environment time: ${info.simulationEnvironment.time}"
      val cumDelay       = s"Cumulative delay: ${info.simulationEnvironment.cumulativeDelay}"
      val avgDelay       = s"Average delay: ${info.simulationEnvironment.averageDelay}"
      val trainInStation = s"Train in stations: ${info.simulationEnvironment.percTrainsInStations} %"
      val trainOnRoute   = s"Train on route: ${info.simulationEnvironment.percTrainsOnRoutes} %"
      val stationLoad    = s"Station load: ${info.simulationEnvironment.percStationsLoad} %"
      val infoStr =
        s"SIMULATION: \n$simuTime \n$envTime \n$cumDelay \n$avgDelay \n$trainInStation \n$trainOnRoute \n$stationLoad"
      infoArea.setText(infoStr)

    override def showStationSimulation(station: StationEnvironmentInfo): Unit =
      val StationEnvironmentInfo(env, cumulativeDelay, averageDelay) = station
      val stationName                                                = s"STATION [${env.name}]:"
      val trains   = s"Trains: ${env.trains.size} / ${env.numberOfPlatforms}"
      val cumDelay = s"Cumulative Delay: $cumulativeDelay"
      val avgDelay = s"Average Delay: $averageDelay "
      val infoStr  = s"$stationName \n$trains \n$cumDelay \n$avgDelay"
      elementInfoArea.setText(infoStr)

    override def showRouteSimulation(route: RouteEnvironmentElement): Unit =
      val stations = s"Stations: ${route.departure.name} - ${route.arrival.name}"
      val trains   = s"Trains: ${route.trains.size}"
      val typology = s"Type: ${route.typology}"
      val infoStr  = s"ROUTE: \n$stations \n$trains \n$typology"
      elementInfoArea.setText(infoStr)

    override def showTrainSimulation(trainInfo: TrainAgentInfo, position: Point2D.Double): Unit =
      val distance = BigDecimal(trainInfo.train.distanceTravelled).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

      val name      = s"TRAIN [${trainInfo.train.name}]:"
      val curDelay  = s"Current Delay: ${trainInfo.delayInCurrentTimetable.getOrElse("No Timetable")}"
      val pos       = s"Position: ${position.x.toInt} - ${position.y.toInt}"
      val travelled = s"Distance Travelled: $distance"
      val speed     = s"Speed: ${trainInfo.train.state.motionData.speed}"
      val infoStr   = s"$name \n$curDelay \n$pos \n$travelled \n$speed"
      elementInfoArea.setText(infoStr)

    override def component[T >: Component]: T = mainPanel
