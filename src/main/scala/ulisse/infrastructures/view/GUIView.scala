package ulisse.infrastructures.view

import ulisse.applications.ports.RoutePorts.Input
import ulisse.infrastructures.view.common.CentralController
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.composed.ComposedImage
import ulisse.infrastructures.view.components.ui.composed.ComposedImage.Direction
import ulisse.infrastructures.view.components.ui.decorators.Styles
import ulisse.infrastructures.view.map.MapPanel

import javax.swing.JLayeredPane
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
    mainLayeredPane.add(dashboardPanel)
    mainLayeredPane.add(menuPanel)

    mapPanel.attach(mapController.stationForm.mapObserver)
    mapPanel.attachItem(mapController.routeForm.mapObserver)

    // Menu panel
    given directionMenu: Direction = Direction.Vertical
    private val newIcon            = ComposedImage.createIconLabel("icons/add.svg", "new")
    private val boxPanel           = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
    private val panel              = ExtendedSwing.JFlowPanelItem()
    panel.contents += newIcon.component
    boxPanel.contents += Swing.VGlue
    boxPanel.contents += panel
    menuPanel.layout(boxPanel) = Center
    // ---------------

    // Dashboard panel
    private val borderPanel            = ExtendedSwing.JBorderPanelItem()
    private val boxDashboardPanelNorth = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
    private val boxDashboardPanelSouth = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)

    private val iconApp             = ComposedImage.createPictureLabel("icons/logo.jpg", "ulisse")
    private val dashboardIconsNorth = for _ <- 1 to 4 yield ComposedImage.createIconLabel("icons/add.svg", "new")
    private val dashboardIconsSouth = for _ <- 1 to 2 yield ComposedImage.createIconLabel("icons/add.svg", "new")

    boxDashboardPanelNorth.contents += ExtendedSwing.createFlowPanel(iconApp.component)
    dashboardIconsNorth.map(icon => ExtendedSwing.createFlowPanel(icon.component))
      .map(panel => { panel.vGap = 10; panel })
      .foreach(boxDashboardPanelNorth.contents += _)

    dashboardIconsSouth.map(icon => ExtendedSwing.createFlowPanel(icon.component))
      .map(panel => { panel.vGap = 10; panel })
      .foreach(boxDashboardPanelSouth.contents += _)

    borderPanel.layout(boxDashboardPanelNorth) = North
    borderPanel.layout(boxDashboardPanelSouth) = South
    dashboardPanel.layout(borderPanel) = West
    // ---------------

//    dashboardPanel.visible = false
//    dashboardPanel.revalidate()
    contents = mainLayeredPane
    mainLayeredPane.revalidate()
