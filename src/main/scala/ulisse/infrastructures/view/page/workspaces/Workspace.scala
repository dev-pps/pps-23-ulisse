package ulisse.infrastructures.view.page.workspaces

import ulisse.applications.AppState
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.manager.FormManager
import ulisse.infrastructures.view.map.MapPanel
import ulisse.infrastructures.view.train.TrainEditorView
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.BorderPanel.Position
import scala.swing.{BorderPanel, Component}

/** Represents the workspace of the application. */
trait Workspace extends ComposedSwing:
  def revalidate(): Unit

object Workspace:
  /** Creates a new instance of simulation workspace. */
  def createSimulation(): SimulationWorkspace = SimulationWorkspace()

  /** Creates a new instance of map workspace. */
  def createMap(formManager: FormManager): MapWorkspace = MapWorkspace(formManager)

  /** Creates a new instance of train workspace. */
  def createTrain(): TrainWorkspace = TrainWorkspace()

  final case class BaseWorkspace() extends Workspace:
    private val mainPanel      = new ExtendedSwing.SLayeredPanel()
    val menuPanel: BorderPanel = BorderPanel().transparent()
    val workPanel: BorderPanel = BorderPanel().transparent()

    mainPanel.add(menuPanel)
    mainPanel.add(workPanel)

    export mainPanel.revalidate

    override def component[T >: Component]: T = mainPanel

  /** Represents the simulation workspace of the application. */
  case class SimulationWorkspace() extends Workspace:
    private val workspace = BaseWorkspace()

    export workspace.{component, revalidate}

  /** Represents the map workspace of the application. */
  final case class MapWorkspace(formManager: FormManager) extends Workspace:
    private val workspace = BaseWorkspace()
    private val mapPanel  = MapPanel.empty()

    workspace.workPanel.layout(mapPanel) = Position.Center
    workspace.menuPanel.layout(formManager.component) = Position.East
    workspace.revalidate()

    export workspace.{component, revalidate}

  /** Represents the train workspace of the application. */
  case class TrainWorkspace() extends Workspace:
    import ulisse.applications.ports.TrainPorts
    import ulisse.applications.useCases.TrainService
    import java.util.concurrent.LinkedBlockingQueue
//
//    val trainPort: TrainPorts.Input = TrainService(LinkedBlockingQueue[AppState => AppState])
//    private val workspace           = TrainEditorView(trainPort)
    private val workspace = BaseWorkspace()
    export workspace.revalidate
    override def component[T >: Component]: T = workspace.component
