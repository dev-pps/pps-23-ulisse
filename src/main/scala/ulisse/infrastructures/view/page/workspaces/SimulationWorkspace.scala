package ulisse.infrastructures.view.page.workspaces

import ulisse.adapters.input.SimulationPageAdapter
import ulisse.entities.simulation.data.SimulationData
import ulisse.infrastructures.view.page.workspaces.Workspace.BaseWorkspace
import ulisse.infrastructures.view.simulation.SimulationNotificationListener

/** Represents the simulation workspace of the application. */
trait SimulationWorkspace extends Workspace with SimulationNotificationListener

/** Companion object of the [[SimulationWorkspace]]. */
object SimulationWorkspace:

  /** Creates a new instance of simulation workspace. */
  def apply(simulationAdapter: SimulationPageAdapter): SimulationWorkspace = SimulationWorkspaceImpl(simulationAdapter)

  /** Represents the simulation workspace of the application. */
  private case class SimulationWorkspaceImpl(simulationAdapter: SimulationPageAdapter) extends SimulationWorkspace:
    private val workspace = BaseWorkspace()
    export workspace.{component, revalidate}

    override def updateData(data: SimulationData): Unit = ()

    override def endSimulation(data: SimulationData): Unit = ()
