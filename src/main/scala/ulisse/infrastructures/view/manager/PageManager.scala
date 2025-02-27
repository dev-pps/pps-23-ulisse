package ulisse.infrastructures.view.manager

import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.map.MapPanel
import ulisse.infrastructures.view.page.Workspace.MapWorkspace
import ulisse.infrastructures.view.page.{Dashboard, Menu}

import scala.swing.BorderPanel.Position
import scala.swing.event.MouseEvent
import scala.swing.{BorderPanel, Component}

/** Represents the page manager of the application. */
trait PageManager extends ComposedSwing:
  /** Shows the menu. */
  def showDashboard(): Unit

  /** Revalidates the page manager. */
  def revalidate(): Unit

object PageManager:

  /** Creates a new instance of the page manager. */
  def apply(): PageManager = PageManagerImpl()

  private case class NewIconEvents(pageManager: PageManager) extends Observers.Observer[MouseEvent]:
    override def onClick(data: MouseEvent): Unit = pageManager.showDashboard()

  private case class PageManagerImpl() extends PageManager:
    private val mainPanel = new ExtendedSwing.LayeredPanel()

    private val menuPanel      = BorderPanel()
    private val dashboardPanel = BorderPanel()

    private val menu             = Menu()
    private val dashboard        = Dashboard()
    private val workspaceManager = WorkspaceManager()

    mainPanel.add(menuPanel)
    mainPanel.add(dashboardPanel)
    mainPanel.add(workspaceManager.component)

    menuPanel.layout(menu.component) = Position.Center
    dashboardPanel.layout(dashboard.component) = Position.West

    menuPanel.opaque = false
    dashboardPanel.opaque = false

    workspaceManager.component.visible = false
    dashboardPanel.visible = false

    menu.attachNewIcon(NewIconEvents(this))

    export mainPanel.revalidate

    override def showDashboard(): Unit =
      dashboardPanel.visible = true
      workspaceManager.component.visible = true
      workspaceManager.showMap()

    override def component[T >: Component]: T = mainPanel
