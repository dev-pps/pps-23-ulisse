package ulisse.infrastructures.view.page.workspaces

import ulisse.adapters.input.SimulationPageAdapter
import ulisse.entities.simulation.data.SimulationData
import ulisse.infrastructures.view.map.MapPanel
import ulisse.infrastructures.view.page.forms.{Form, SimulationForm}
import ulisse.infrastructures.view.page.workspaces.Workspace.BaseWorkspace
import ulisse.infrastructures.view.simulation.SimulationNotificationListener
import ulisse.infrastructures.view.utils.Swings.given_ExecutionContext

import scala.swing.BorderPanel.Position

/** Represents the simulation workspace of the application. */
trait SimulationWorkspace extends Workspace with SimulationNotificationListener:
  /** Updates the simulation data. */
  def initSimulation(): Unit

/** Companion object of the [[SimulationWorkspace]]. */
object SimulationWorkspace:

  /** Creates a new instance of simulation workspace. */
  def apply(simulationAdapter: SimulationPageAdapter): SimulationWorkspace = SimulationWorkspaceImpl(simulationAdapter)

  /** Represents the simulation workspace of the application. */
  private case class SimulationWorkspaceImpl(adapter: SimulationPageAdapter) extends SimulationWorkspace:
    private val workspace = BaseWorkspace()

    private val mapPanel: MapPanel         = MapPanel()
    private val simulation: SimulationForm = Form.createSimulation()

    workspace.workPanel.layout(mapPanel) = Position.Center
    workspace.menuPanel.layout(simulation.component) = Position.East

    simulation.attachStartSimulation(SimulationForm.PlaySimulationEvent(adapter))
    simulation.attachResetSimulation(SimulationForm.ResetSimulationEvent(adapter))

    export workspace.{component, revalidate}

    override def initSimulation(): Unit =
      val values = adapter.initSimulation()
      values.onComplete(_.fold(
        error => println(s"Error: $error"),
        (engine, data) => data
      ))
      println("Initializing simulation:")

    override def updateData(data: SimulationData): Unit =
      println("Updating simulation data:")

    override def endSimulation(data: SimulationData): Unit =
      println("Ending simulation:")
