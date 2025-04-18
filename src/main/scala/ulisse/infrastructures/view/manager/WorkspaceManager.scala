package ulisse.infrastructures.view.manager

import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.page.workspaces.Workspace.TrainWorkspace
import ulisse.infrastructures.view.page.workspaces.{MapWorkspace, SimulationWorkspace, Workspace}
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.BorderPanel.Position
import scala.swing.{BorderPanel, Component}

/** Manages the workspaces of the application. */
trait WorkspaceManager extends ComposedSwing:
  /** Shows the simulation workspace. */
  def showSimulation(): Unit

  /** Shows the map workspace. */
  def showMap(): Unit

  /** Shows the train workspace. */
  def showTrain(): Unit

  /** Resets the simulation. */
  def resetSimulation(): Unit

object WorkspaceManager:

  /** Creates a new instance of the workspace manager with the given [[SimulationWorkspace]], [[MapWorkspace]], and [[TrainWorkspace]]. */
  def apply(
      simulationWorkspace: SimulationWorkspace,
      mapWorkspace: MapWorkspace,
      trainWorkspace: TrainWorkspace
  ): WorkspaceManager =
    WorkspaceManagerImpl(simulationWorkspace, mapWorkspace, trainWorkspace)

  private case class WorkspaceManagerImpl(simulation: SimulationWorkspace, map: MapWorkspace, train: TrainWorkspace)
      extends WorkspaceManager:
    private val mainPanel  = BorderPanel().transparent()
    private val workspaces = List(simulation, map, train)

    showTrain()
    showMap()
    showSimulation()

    private def showWorkspace(workspace: Workspace): Unit =
      mainPanel.layout(workspace.component) = Position.Center
      workspaces.foreach(_.hide())
      workspace.show()
      workspace.revalidate()
      mainPanel.validate()

    override def showSimulation(): Unit = { showWorkspace(simulation); simulation.initSimulation() }
    override def showMap(): Unit        = { showWorkspace(map); map.initMap() }
    override def showTrain(): Unit      = showWorkspace(train)

    override def resetSimulation(): Unit = simulation.resetSimulation()

    override def component[T >: Component]: T = mainPanel
