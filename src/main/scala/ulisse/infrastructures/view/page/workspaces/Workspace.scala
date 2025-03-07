package ulisse.infrastructures.view.page.workspaces

import ulisse.adapters.InputAdapterManager
import ulisse.adapters.input.SimulationPageAdapter
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.{BorderPanel, Component}

/** Represents the workspace of the application. */
trait Workspace extends ComposedSwing:
  def revalidate(): Unit

/** Companion object of the [[Workspace]]. */
object Workspace:
  /** Creates a new instance of simulation workspace. */
  def createSimulation(simulationAdapter: SimulationPageAdapter): SimulationWorkspace =
    SimulationWorkspace(simulationAdapter)

  /** Creates a new instance of map workspace. */
  def createMap(adapterManager: InputAdapterManager): MapWorkspace = MapWorkspace(adapterManager)

  /** Creates a new instance of train workspace. */
  def createTrain(adapterManager: InputAdapterManager): TrainWorkspace = TrainWorkspace(adapterManager)

  final case class BaseWorkspace() extends Workspace:
    private val mainPanel      = new ExtendedSwing.SLayeredPanel()
    val menuPanel: BorderPanel = BorderPanel().transparent()
    val workPanel: BorderPanel = BorderPanel().transparent()

    mainPanel.add(menuPanel)
    mainPanel.add(workPanel)

    export mainPanel.revalidate

    override def component[T >: Component]: T = mainPanel

  /** Represents the train workspace of the application. */
  case class TrainWorkspace(adapterManager: InputAdapterManager) extends Workspace:
//
//    val trainPort: TrainPorts.Input = TrainService(LinkedBlockingQueue[AppState => AppState])
//    private val workspace           = TrainEditorView(trainPort)
    private val workspace = BaseWorkspace()
    export workspace.revalidate
    override def component[T >: Component]: T = workspace.component
