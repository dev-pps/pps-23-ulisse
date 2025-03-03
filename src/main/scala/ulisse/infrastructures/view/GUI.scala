package ulisse.infrastructures.view

import ulisse.applications.InputPortManager
import ulisse.infrastructures.view.manager.{FormManager, PageManager, WorkspaceManager}
import ulisse.infrastructures.view.page.forms.Form
import ulisse.infrastructures.view.page.workspaces.Workspace.{MapWorkspace, SimulationWorkspace, TrainWorkspace}
import ulisse.infrastructures.view.page.{Dashboard, Menu}
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.BorderPanel.Position.*
import scala.swing.{Dimension, MainFrame}

trait GUI

object GUI:
  def apply(inputPortManager: InputPortManager): GUI = GUIImpl(inputPortManager)

  private case class GUIImpl(inputPortManager: InputPortManager) extends MainFrame, GUI:
    title = "Ulisse"
    preferredSize = new Dimension(1600, 1000)

    private val stationForm  = Form.createStation()
    private val routeForm    = Form.createRoute()
    private val scheduleForm = Form.createSchedule()

    private val mapFormManager = FormManager.createMap(stationForm, routeForm, scheduleForm)

    private val simulationWorkspace = SimulationWorkspace()
    private val mapWorkspace        = MapWorkspace(mapFormManager)
    private val trainWorkspace      = TrainWorkspace()

    private val menu             = Menu()
    private val dashboard        = Dashboard()
    private val workspaceManager = WorkspaceManager(simulationWorkspace, mapWorkspace, trainWorkspace)

    private val pageManager = PageManager(menu, dashboard, workspaceManager)

    contents = pageManager.component
    pageManager.revalidate()
    resizable = false
    visible = true
