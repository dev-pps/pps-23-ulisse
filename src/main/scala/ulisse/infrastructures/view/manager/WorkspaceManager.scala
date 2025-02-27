package ulisse.infrastructures.view.manager

import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.page.Workspace.{MapWorkspace, SimulationWorkspace, TrainWorkspace}
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.BorderPanel.Position
import scala.swing.{BorderPanel, Component}

/** Manages the workspaces of the application. */
trait WorkspaceManager extends ComposedSwing:
//  /** The simulation workspace. */
//  def simulation: SimulationWorkspace
//
//  /** The map workspace. */
//  def map: MapWorkspace
//
//  /** The train workspace. */
//  def train: TrainWorkspace

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
    private val mainPanel               = BorderPanel().transparent()
    val simulation: SimulationWorkspace = SimulationWorkspace()
    val map: MapWorkspace               = MapWorkspace()
    val train: TrainWorkspace           = TrainWorkspace()

    mainPanel.layout(map.component) = Position.Center

    override def showSimulation(): Unit =
      mainPanel.layout(simulation.component) = Position.Center
      simulation.revalidate()

    override def showMap(): Unit =
      mainPanel.layout(map.component) = Position.Center
      map.revalidate()

    override def showTrain(): Unit =
      mainPanel.layout(train.component) = Position.Center
      train.revalidate()

    override def component[T >: Component]: T = mainPanel
