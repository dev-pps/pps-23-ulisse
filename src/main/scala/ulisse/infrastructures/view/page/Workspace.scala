package ulisse.infrastructures.view.page

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
  def createMap(): MapWorkspace = MapWorkspace()

  /** Creates a new instance of train workspace. */
  def createTrain(): TrainWorkspace = TrainWorkspace()

  private case class BaseWorkspace() extends Workspace:
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
  case class MapWorkspace() extends Workspace:
    private val workspace   = BaseWorkspace()
    private val mapPanel    = MapPanel.empty()
    private val formManager = FormManager.createMap()

    workspace.workPanel.layout(mapPanel) = Position.Center
    workspace.menuPanel.layout(formManager.component) = Position.East
    workspace.revalidate()

    export workspace.{component, revalidate}

  /** Represents the train workspace of the application. */
  case class TrainWorkspace() extends Workspace:
    import ulisse.applications.ports.TrainPorts
    import ulisse.applications.useCases.TrainService
    import java.util.concurrent.LinkedBlockingQueue
    import ulisse.applications.managers.TechnologyManagers.TechnologyManager
    import ulisse.applications.managers.TrainManagers.TrainManager
    import ulisse.entities.train.Trains.TrainTechnology

    private type AppState = (TrainManager, TechnologyManager[TrainTechnology])
    val trainPort: TrainPorts.Input = TrainService(LinkedBlockingQueue[AppState => AppState])
    private val workspace           = TrainEditorView(trainPort)
    export workspace.revalidate
    override def component[T >: Component]: T = workspace
