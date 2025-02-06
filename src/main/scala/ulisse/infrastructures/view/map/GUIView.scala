package ulisse.infrastructures.view.map

import ulisse.applications.ports.RoutePorts.UIInputPort
import ulisse.infrastructures.view.components.JStyler.*
import ulisse.infrastructures.view.form.CentralController

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

    private val countLabel = "Count Route: "
    private val errorStr   = "Error: "

    val mapPark: MapPanel = MapPanel.empty()
    val mapController     = CentralController.createMap()

    // Main content pane with BorderLayout
    val mainPane  = BorderPanel()
    val glassPane = BorderPanel()

    glassPane.opaque = false
    glassPane.visible = true

    mainPane.layout(mapPark) = Center
    glassPane.layout(mapController.component) = East

    contents = mainPane
    peer.setGlassPane(glassPane.peer)
    glassPane.visible = true

    // formPanel button action
//    formPanel.saveButton.reactions += {
//      case event.ButtonClicked(_) =>
//        uiPort.save(formPanel.create()).onComplete(future =>
//          for {
//            either <- future
//          } yield {
//            either match
//              case Left(errorSaving) => error.text = s"$errorStr + $errorSaving"
//              case Right(routes) =>
//                mapPark.setPoints(routes.map(router => router.path))
//                mapPark.repaint()
//                info.text = s"$countLabel ${routes.size}"
//          }
//        )
//    }

//    @SuppressWarnings(Array("org.wartremover.warts.Var"))
//    private var field = 0
//    mapPark.listenTo(mapPark.mouse.clicks)
//    mapPark.reactions += {
//      case event.MousePressed(_, point, _, _, _) =>
//        if formPanel.visible then
//          field match
//            case 0 => formPanel.setDepartureStation("station", point.getX, point.getY)
//            case 1 => formPanel.setArrivalStation("station", point.getX, point.getY)
//          field = (field + 1) % 2
//    }
