package ulisse.infrastructures.view.page.workspaces

import ulisse.adapters.input.{SimulationInfoAdapter, SimulationPageAdapter}
import ulisse.adapters.output.SimulationNotificationListener
import ulisse.applications.ports.SimulationPorts.Input.Preset
import ulisse.entities.simulation.data.SimulationData
import ulisse.infrastructures.view.map.MapSimulation
import ulisse.infrastructures.view.page.forms.SimulationForm
import ulisse.infrastructures.view.page.workspaces.Workspace.BaseWorkspace
import ulisse.infrastructures.view.utils.Swings.given_ExecutionContext
import ulisse.utils.Times.*

import scala.swing.BorderPanel.Position
import scala.swing.Swing

/** Represents the simulation workspace of the application. */
trait SimulationWorkspace extends Workspace with SimulationNotificationListener:
  /** Updates the simulation data. */
  def initSimulation(): Unit

  /** Resets the simulation. */
  def resetSimulation(): Unit

/** Companion object of the [[SimulationWorkspace]]. */
object SimulationWorkspace:

  /** Creates a new instance of simulation workspace. */
  def apply(simulationAdapter: SimulationPageAdapter, infoSimulation: SimulationInfoAdapter): SimulationWorkspace =
    SimulationWorkspaceImpl(simulationAdapter, infoSimulation)

  /** Represents the simulation workspace of the application. */
  private case class SimulationWorkspaceImpl(adapter: SimulationPageAdapter, infoSimulation: SimulationInfoAdapter)
      extends SimulationWorkspace:
    private val workspace                  = BaseWorkspace()
    private val mapPanel: MapSimulation    = MapSimulation()
    private val simulation: SimulationForm = SimulationForm()

    workspace.workPanel.layout(mapPanel) = Position.Center
    workspace.menuPanel.layout(simulation.component) = Position.East

    simulation attachStartSimulation (SimulationForm PlaySimulationEvent (adapter, this, simulation))
    simulation attachResetSimulation (SimulationForm ResetSimulationEvent (adapter, this, simulation))
    simulation attachSimpleSimulation (SimulationForm SetupSimulationEvent (adapter, this, simulation, Preset.Simple))
    simulation attachNormalSimulation (SimulationForm SetupSimulationEvent (adapter, this, simulation, Preset.Complex))
    simulation attachComplexSimulation (SimulationForm SetupSimulationEvent (adapter, this, simulation, Preset.Examples))

    export workspace.{component, revalidate}

    override def initSimulation(): Unit =
      adapter.initSimulation() onComplete (_.fold(
        error => println(s"Error: $error"),
        (engine, data) =>
          updateData(data)
          simulation setEngineConfiguration engine.configuration
      ))

    override def resetSimulation(): Unit =
      simulation.reset()

    private def refreshMap(data: SimulationData): Unit =
      Swing.onEDT:
        mapPanel updateStation data.simulationEnvironment.stations
        mapPanel updateRoutes data.simulationEnvironment.routes
        mapPanel updateTrain data.simulationEnvironment.routes
        mapPanel attachClickStation SimulationForm.TakeStationEvent(simulation, infoSimulation)
        mapPanel attachClickRoute SimulationForm.TakeRouteEvent(simulation, infoSimulation)
        mapPanel attachClickTrain SimulationForm.TakeTrainEvent(simulation, infoSimulation)
        simulation.showSimulationData(data)

    override def updateData(data: SimulationData): Unit =
      refreshMap(data)

    override def endSimulation(data: SimulationData): Unit =
      refreshMap(data)
      adapter.reset()
      simulation.reset()
