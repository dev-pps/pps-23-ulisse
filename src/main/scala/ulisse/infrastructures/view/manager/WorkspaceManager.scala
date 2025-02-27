package ulisse.infrastructures.view.manager

import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.page.Workspace.{MapWorkspace, SimulationWorkspace, TrainWorkspace}
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.BorderPanel.Position
import scala.swing.{BorderPanel, Component, FlowPanel}

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

    override def showSimulation(): Unit =
      mainPanel.layout(simulation.component) = Position.Center
      mainPanel.validate()
      simulation.revalidate()

    override def showMap(): Unit =
      mainPanel.layout(map.component) = Position.Center
      mainPanel.validate()
      map.revalidate()

    override def showTrain(): Unit =
      mainPanel.layout(train.component) = Position.Center
      mainPanel.validate()
      train.revalidate()

    override def component[T >: Component]: T = mainPanel
