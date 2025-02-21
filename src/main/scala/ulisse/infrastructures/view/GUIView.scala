package ulisse.infrastructures.view

import ulisse.applications.ports.RoutePorts.Input
import ulisse.infrastructures.view.common.CentralController
import ulisse.infrastructures.view.components.Styles.*
import ulisse.infrastructures.view.map.MapPanel

import scala.concurrent.ExecutionContext
import scala.swing.*
import scala.swing.BorderPanel.Position.*

given ExecutionContext = ExecutionContext.fromExecutor: (runnable: Runnable) =>
  Swing.onEDT(runnable.run())

trait GUIView

object GUIView:
  def apply(uiPort: Input): GUIView = GUIViewImpl(uiPort)

  private case class GUIViewImpl(uiPort: Input) extends MainFrame, GUIView:
    title = "Map"
    visible = true
    preferredSize = new Dimension(800, 800)

    private val mainPane  = BorderPanel()
    private val glassPane = BorderPanel()

    private val mapController = CentralController.createMap()
    private val mapPanel      = MapPanel.empty()

    mapPanel.attach(mapController.stationForm.mapObserver)
    mapPanel.attachItem(mapController.routeForm.mapObserver)

    glassPane.opaque = false
    glassPane.visible = true

    mainPane.layout(mapPanel) = Center
    glassPane.layout(mapController.component) = East

    contents = mainPane
    peer.setGlassPane(glassPane.peer)
    glassPane.visible = true
