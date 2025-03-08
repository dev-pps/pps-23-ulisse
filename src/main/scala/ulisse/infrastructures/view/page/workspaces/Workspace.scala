package ulisse.infrastructures.view.page.workspaces

import ulisse.adapters.InputAdapterManager
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.manager.FormManager
import ulisse.infrastructures.view.map.MapPanel
import ulisse.infrastructures.view.train.TrainEditorView
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.{BorderPanel, Component}

/** Represents the workspace of the application. */
trait Workspace extends ComposedSwing:
  def revalidate(): Unit

/** Companion object of the [[Workspace]]. */
object Workspace:

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
    private val workspace = TrainEditorView(adapterManager.train)
    export workspace.revalidate
    override def component[T >: Component]: T = workspace
