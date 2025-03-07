package ulisse.infrastructures.view

import ulisse.adapters.InputAdapterManager
import ulisse.infrastructures.view.manager.{PageManager, WorkspaceManager}
import ulisse.infrastructures.view.page.workspaces.MapWorkspace
import ulisse.infrastructures.view.page.workspaces.Workspace.{SimulationWorkspace, TrainWorkspace}
import ulisse.infrastructures.view.page.{Dashboard, Menu}
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.BorderPanel.Position.*
import scala.swing.{Dimension, MainFrame}

trait GUI

object GUI:
  def apply(adapterManager: InputAdapterManager, workspace: SimulationWorkspace): GUI =
    GUIImpl(adapterManager, workspace)

  private case class GUIImpl(adapterManager: InputAdapterManager, workspace: SimulationWorkspace) extends MainFrame,
        GUI:
    title = "Ulisse"
    preferredSize = new Dimension(1600, 1000)

    private val mapWorkspace   = MapWorkspace(adapterManager)
    private val trainWorkspace = TrainWorkspace(adapterManager)

    private val menu             = Menu()
    private val dashboard        = Dashboard()
    private val workspaceManager = WorkspaceManager(workspace, mapWorkspace, trainWorkspace)

    private val pageManager = PageManager(menu, dashboard, workspaceManager)

    contents = pageManager.component
    pageManager.revalidate()
    resizable = false
    visible = true
