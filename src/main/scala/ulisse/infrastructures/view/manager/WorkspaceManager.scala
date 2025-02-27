package ulisse.infrastructures.view.manager

import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.page.Workspace
import ulisse.infrastructures.view.page.Workspace.{MapWorkspace, SimulationWorkspace, TrainWorkspace}
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

object WorkspaceManager:

  /** Creates a new instance of the workspace manager. */
  def apply(): WorkspaceManager = WorkspaceManagerImpl()

  private case class WorkspaceManagerImpl() extends WorkspaceManager:
    private val mainPanel                       = BorderPanel().transparent()
    private val simulation: SimulationWorkspace = SimulationWorkspace()
    private val map: MapWorkspace               = MapWorkspace()
    private val train: TrainWorkspace           = TrainWorkspace()
    private val workspaces                      = List(simulation, map, train)

    showSimulation()
    showTrain()
    showMap()

    private def showWorkspace(workspace: Workspace): Unit =
      mainPanel.layout(workspace.component) = Position.Center
      workspaces.foreach(_.hide())
      workspace.show()
      workspace.revalidate()
      mainPanel.validate()

    override def showSimulation(): Unit = showWorkspace(simulation)
    override def showMap(): Unit        = showWorkspace(map)
    override def showTrain(): Unit      = showWorkspace(train)

    override def component[T >: Component]: T = mainPanel
