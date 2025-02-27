package ulisse.infrastructures.view.manager

import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.page.Workspace.{MapWorkspace, SimulationWorkspace, TrainWorkspace}

import scala.swing.BorderPanel.Position
import scala.swing.{BorderPanel, Component}

/** Manages the workspaces of the application. */
trait WorkspaceManager extends ComposedSwing:
  /** The simulation workspace. */
  def simulation: SimulationWorkspace

  /** The map workspace. */
  def map: MapWorkspace

  /** The train workspace. */
  def train: TrainWorkspace

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
    private val mainPanel                        = BorderPanel()
    override val simulation: SimulationWorkspace = SimulationWorkspace()
    override val map: MapWorkspace               = MapWorkspace()
    override val train: TrainWorkspace           = TrainWorkspace()

    mainPanel.opaque = false
    mainPanel.layout(map.component) = Position.Center

    override def showSimulation(): Unit =
      mainPanel.revalidate()
      mainPanel.repaint()

    override def showMap(): Unit =
      map.revalidate()
      mainPanel.revalidate()

    override def showTrain(): Unit =
      mainPanel.revalidate()
      mainPanel.repaint()

    override def component[T >: Component]: T = mainPanel
