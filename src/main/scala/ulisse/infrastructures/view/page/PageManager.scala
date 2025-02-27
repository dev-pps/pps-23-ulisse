package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.map.MapPanel

import javax.swing.JLayeredPane
import scala.swing.BorderPanel.Position
import scala.swing.event.MouseEvent
import scala.swing.{BorderPanel, Component}

/** Represents the page manager of the application. */
trait PageManager extends ComposedSwing:
  /** Shows the menu. */
  def showMenu(): Unit

  /** Revalidates the page manager. */
  def revalidate(): Unit

object PageManager:

  /** Creates a new instance of the page manager. */
  def apply(): PageManager = PageManagerImpl()

  private case class NewIconEvents(pageManager: PageManager) extends Observers.Observer[MouseEvent]:
    override def onClick(data: MouseEvent): Unit = pageManager.showMenu()

  private case class PageManagerImpl() extends PageManager:
    private val mainPanel = new ExtendedSwing.LayeredPanel()

    private val dashboardPanel = BorderPanel()
    private val menuPanel      = BorderPanel()
    private val mapPanel       = MapPanel.empty()

    private val menu          = Menu()
    private val dashboard     = Dashboard()
    private val mapController = CentralController.createMap()

    mainPanel.add(dashboardPanel, JLayeredPane.PALETTE_LAYER)
    mainPanel.add(menuPanel, JLayeredPane.DEFAULT_LAYER)

    dashboardPanel.layout(dashboard.component) = Position.Center
    menuPanel.layout(menu.component) = Position.West

    dashboardPanel.opaque = false
    menuPanel.opaque = false
    menuPanel.visible = false
    mainPanel.revalidate()

    dashboard.attachNewIcon(NewIconEvents(this))

    export mainPanel.revalidate

    override def showMenu(): Unit = menuPanel.visible = true

    override def component[T >: Component]: T = mainPanel
