package ulisse.infrastructures.view.manager

import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.map.MapPanel
import ulisse.infrastructures.view.page.{Dashboard, Menu}

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

    private val menuPanel      = BorderPanel()
    private val dashboardPanel = BorderPanel()
    private val mapPanel       = MapPanel.empty()

    private val menu          = Menu()
    private val dashboard     = Dashboard()
    private val mapController = FormManager.createMap()

    mainPanel.add(menuPanel, JLayeredPane.PALETTE_LAYER)
    mainPanel.add(dashboardPanel, JLayeredPane.DEFAULT_LAYER)

    menuPanel.layout(menu.component) = Position.Center
    dashboardPanel.layout(dashboard.component) = Position.West

    menuPanel.opaque = false
    dashboardPanel.opaque = false
    dashboardPanel.visible = false
    mainPanel.revalidate()

    menu.attachNewIcon(NewIconEvents(this))

    export mainPanel.revalidate

    override def showMenu(): Unit = dashboardPanel.visible = true

    override def component[T >: Component]: T = mainPanel
