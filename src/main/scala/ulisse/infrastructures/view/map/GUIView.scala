package ulisse.infrastructures.view.map

import ulisse.applications.ports.RoutePorts.UIInputPort
import ulisse.infrastructures.view.common.CentralController
import ulisse.infrastructures.view.components.JStyler.*

import scala.concurrent.ExecutionContext
import scala.swing.*
import scala.swing.BorderPanel.Position.*

given ExecutionContext = ExecutionContext.fromExecutor: (runnable: Runnable) =>
  Swing.onEDT(runnable.run())

trait GUIView

object GUIView:
  def apply(uiPort: UIInputPort): GUIView = GUIViewImpl(uiPort)

  private case class GUIViewImpl(uiPort: UIInputPort) extends MainFrame, GUIView:
    title = "Map"
    visible = true
    preferredSize = new Dimension(800, 800)

    private val mainPane  = BorderPanel()
    private val glassPane = BorderPanel()

    private val mapPark       = MapPanel.empty()
    private val mapController = CentralController.createMap()

    glassPane.opaque = false
    glassPane.visible = true

    mainPane.layout(mapPark) = Center
    glassPane.layout(mapController.component) = East

    contents = mainPane
    peer.setGlassPane(glassPane.peer)
    glassPane.visible = true
