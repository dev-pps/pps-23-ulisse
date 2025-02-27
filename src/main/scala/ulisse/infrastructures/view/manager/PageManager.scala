package ulisse.infrastructures.view.manager

import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.page.{Dashboard, Menu}
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.Component
import scala.swing.event.MouseEvent

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
    private val mainPanel        = new ExtendedSwing.LayeredPanel()
    private val menu             = Menu()
    private val dashboard        = Dashboard()
    private val workspaceManager = WorkspaceManager()

    mainPanel.add(menu.component)
    mainPanel.add(dashboard.component)
    mainPanel.add(workspaceManager.component)

    dashboard.hide()
    workspaceManager.hide()

    menu.attachNewIcon(NewIconEvents(this))

    export mainPanel.revalidate

    override def showDashboard(): Unit =
      dashboard.show()
      workspaceManager.show()
      workspaceManager.showMap()

    override def component[T >: Component]: T = mainPanel
