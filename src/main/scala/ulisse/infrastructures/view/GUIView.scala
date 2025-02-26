package ulisse.infrastructures.view

import ulisse.applications.ports.RoutePorts.Input
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.utils.ComponentUtils.*
import ulisse.infrastructures.view.map.MapPanel
import ulisse.infrastructures.view.page.{CentralController, Dashboard}
import ulisse.infrastructures.view.page.Menu

import scala.swing.*
import scala.swing.BorderPanel.Position.*

trait GUIView

object GUIView:
  def apply(uiPort: Input): GUIView = GUIViewImpl(uiPort)

  private case class GUIViewImpl(uiPort: Input) extends MainFrame, GUIView:
    title = "Map"
    visible = true
    preferredSize = new Dimension(1000, 1000)

    private val mainLayeredPane = new ExtendedSwing.LayeredPanel()
    private val pageLayeredPane = new ExtendedSwing.LayeredPanel()

    /** Menu panel, contains primary actions (new, import, ...). */
    private val menuPanel = ExtendedSwing.JBorderPanelItem()
    menuPanel.rectPalette = Styles.transparentPalette

    /** Dashboard panel, contains simulation, map, ... */
    private val dashboardPanel = ExtendedSwing.JBorderPanelItem()
    dashboardPanel.rectPalette = Styles.transparentPalette

    /** Map controller, contains map and route form. */
    private val mapController = CentralController.createMap()

    /** Map panel, contains elements graphic. */
    private val mapPanel = MapPanel.empty()

//    pageLayeredPane.add(mapPanel, JLayeredPane.DEFAULT_LAYER)
//    pageLayeredPane.add(mapController.component, JLayeredPane.DEFAULT_LAYER)

//    mainLayeredPane.add(pageLayeredPane, JLayeredPane.DEFAULT_LAYER)
//    mainLayeredPane.add(dashboardPanel)
    mainLayeredPane.add(menuPanel)

    mapPanel.attach(mapController.stationForm.mapObserver)
    mapPanel.attachItem(mapController.routeForm.mapObserver)

    // Menu panel
    private val menu = Menu()
    menuPanel.layout(menu.component) = West

    // Dashboard panel
    private val dashboard = Dashboard()
    dashboardPanel.layout(dashboard.component) = Center

    contents = mainLayeredPane
    mainLayeredPane.revalidate()
