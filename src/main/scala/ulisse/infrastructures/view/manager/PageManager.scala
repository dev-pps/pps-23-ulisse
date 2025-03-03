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
  /** Shows the dashboard. */
  def showDashboard(): Unit

  /** Show the simulation workspace. */
  def showSimulation(): Unit

  /** Show the map workspace. */
  def showMap(): Unit

  /** Show the train workspace. */
  def showTrain(): Unit

  /** Revalidates the page manager. */
  def revalidate(): Unit

object PageManager:

  /** Creates a new instance of the page manager with the given [[Menu]], [[Dashboard]], and [[WorkspaceManager]]. */
  def apply(menu: Menu, dashboard: Dashboard, workspaceManager: WorkspaceManager): PageManager =
    PageManagerImpl(menu, dashboard, workspaceManager)

  private case class NewButtonEvents(pageManager: PageManager) extends Observers.Observer[MouseEvent]:
    override def onClick(data: MouseEvent): Unit = pageManager.showDashboard()

  private case class SimulationButtonEvents(pageManager: PageManager) extends Observers.Observer[MouseEvent]:
    override def onClick(data: MouseEvent): Unit = pageManager.showSimulation()

  private case class MapButtonEvents(pageManager: PageManager) extends Observers.Observer[MouseEvent]:
    override def onClick(data: MouseEvent): Unit = pageManager.showMap()

  private case class TrainButtonEvents(pageManager: PageManager) extends Observers.Observer[MouseEvent]:
    override def onClick(data: MouseEvent): Unit = pageManager.showTrain()

  private case class PageManagerImpl(menu: Menu, dashboard: Dashboard, workspaceManager: WorkspaceManager)
      extends PageManager:

    private val mainPanel = new ExtendedSwing.SLayeredPanel()

    mainPanel.add(menu.component)
    mainPanel.add(dashboard.component)
    mainPanel.add(workspaceManager.component)

    dashboard.hide()
    workspaceManager.hide()

    menu.attachNewIcon(NewButtonEvents(this))
    dashboard.attachSimulation(SimulationButtonEvents(this))
    dashboard.attachMap(MapButtonEvents(this))
    dashboard.attachTrain(TrainButtonEvents(this))

    export mainPanel.revalidate

    override def showDashboard(): Unit =
      dashboard.show()
      workspaceManager.show()

    override def showSimulation(): Unit = workspaceManager.showSimulation()
    override def showMap(): Unit        = workspaceManager.showMap()
    override def showTrain(): Unit      = workspaceManager.showTrain()

    override def component[T >: Component]: T = mainPanel
