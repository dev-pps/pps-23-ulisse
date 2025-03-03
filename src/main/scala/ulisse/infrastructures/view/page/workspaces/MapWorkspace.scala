package ulisse.infrastructures.view.page.workspaces

import ulisse.adapters.InputAdapterManager
import ulisse.infrastructures.view.manager.FormManager
import ulisse.infrastructures.view.map.MapPanel
import ulisse.infrastructures.view.page.workspaces.Workspace.BaseWorkspace

import scala.swing.BorderPanel.Position

/** Represents the map workspace of the application. */
trait MapWorkspace extends Workspace

/** Companion object of the [[MapWorkspace]]. */
object MapWorkspace:

  /** Creates a new instance of map workspace. */
  def apply(adapterManager: InputAdapterManager): MapWorkspace =
    MapWorkspaceImpl(adapterManager)

  /** Represents the map workspace of the application. */
  private final case class MapWorkspaceImpl(adapterManager: InputAdapterManager) extends MapWorkspace:
    private val workspace   = BaseWorkspace()
    private val mapPanel    = MapPanel.empty()
    private val formManager = FormManager.createMap()

    workspace.workPanel.layout(mapPanel) = Position.Center
    workspace.menuPanel.layout(formManager.component) = Position.East
    workspace.revalidate()

    export workspace.{component, revalidate}
