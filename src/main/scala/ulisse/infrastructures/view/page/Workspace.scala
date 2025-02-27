package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.manager.FormManager

import javax.swing.JLayeredPane
import scala.swing.BorderPanel.Position
import scala.swing.{BorderPanel, Component}

/** Represents the workspace of the application. */
trait Workspace extends ComposedSwing

object Workspace:
  /** Creates a new instance of simulation workspace. */
  def createSimulation(): SimulationWorkspace = SimulationWorkspace()

  /** Creates a new instance of map workspace. */
  def createMap(): MapWorkspace = MapWorkspace()

  /** Creates a new instance of train workspace. */
  def createTrain(): TrainWorkspace = TrainWorkspace()

  private case class BaseWorkspace() extends Workspace:
    private val mainPanel = new ExtendedSwing.LayeredPanel()
    val workPanel         = BorderPanel()
    val menuPanel         = BorderPanel()

    menuPanel.opaque = false
    workPanel.opaque = false

    mainPanel.add(workPanel, JLayeredPane.DEFAULT_LAYER)
    mainPanel.add(menuPanel, JLayeredPane.PALETTE_LAYER)

    override def component[T >: Component]: T = mainPanel

  /** Represents the simulation workspace of the application. */
  case class SimulationWorkspace() extends Workspace:
    private val workspace = BaseWorkspace()

    export workspace.component

  /** Represents the map workspace of the application. */
  case class MapWorkspace() extends Workspace:
    private val workspace   = BaseWorkspace()
    private val formManager = FormManager.createMap()

    workspace.menuPanel.layout(formManager.component) = Position.East

    export workspace.component

  /** Represents the train workspace of the application. */
  case class TrainWorkspace() extends Workspace:
    private val workspace = BaseWorkspace()

    export workspace.component
