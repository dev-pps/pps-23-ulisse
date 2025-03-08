package ulisse.infrastructures.view.page.forms

import ulisse.adapters.input.SimulationPageAdapter
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
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.{EnhancedLook, ShapeEffect}
import ulisse.utils.Times.*

import javax.swing.JTextArea
import scala.swing.BorderPanel.Position
import scala.swing.{Component, Swing}

/** Represents the simulation form of the application. */
trait SimulationForm extends Form:

  /** Attach the start simulation observer to the form of type [[SimulationInfo]]. */
  def attachStartSimulation(observer: ClickObserver[SimulationInfo]): Unit

  /** Attach the reset simulation observer to the form of type [[Unit]]. */
  def attachResetSimulation(observer: ClickObserver[Unit]): Unit

  /** Sets the simulation information. */
  def setEngineConfiguration(engine: EngineConfiguration): Unit

  /** Prints the simulation information. */
  def printInfoSimulation(info: SimulationData): Unit

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

  private case class SimulationFormImpl() extends SimulationForm:
    private val mainPanel: SBorderPanel                     = SBorderPanel()
    private val stepSize: ComposedSwing.InfoTextField       = ComposedSwing createInfoTextField "Step"
    private val cyclePerSecond: ComposedSwing.InfoTextField = ComposedSwing createInfoTextField "Cycle"
    private val playButton   = ExtendedSwing createFormButtonWith ("Play", Styles.formButtonRect)
    private val resetButton  = ExtendedSwing createFormButtonWith ("Reset", Styles.formButtonRect)
    private val textInfoArea = ExtendedSwing.STextArea()
    private val form         = BaseForm("Simulation", stepSize, cyclePerSecond)

    private val panelInfo = ExtendedSwing createFlowPanel (Component.wrap(textInfoArea), form.component)
    panelInfo.contents += Component.wrap(textInfoArea)

    buttonPanel.contents += playButton
    buttonPanel.contents += resetButton

    mainPanel.layout(panelInfo) = Position.North
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

    override def printInfoSimulation(info: SimulationData): Unit =
      val infoStr = s"""SIMULATION TIME: ${info.millisecondsElapsed.toTime}
         \nENVIRONMENT TIME: ${info.simulationEnvironment.time}
         \nCUMULATIVE DELAY: ${info.simulationEnvironment.cumulativeDelay}
         \nAVERAGE DELAY: ${info.simulationEnvironment.averageDelay}
         \nTRAIN IN STATIONS: ${info.simulationEnvironment.percTrainsInStations} %
         \nTRAIN ON ROUTE: ${info.simulationEnvironment.percTrainsOnRoutes} %
         \nSTATION LOAD: ${info.simulationEnvironment.percStationsLoad} %"""
      textInfoArea.setText(infoStr)

    override def component[T >: Component]: T = mainPanel
