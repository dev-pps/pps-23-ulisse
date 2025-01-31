package ulisse.infrastructures.view.map

import ulisse.applications.ports.RoutePorts.UIPort
import ulisse.applications.useCases.RouteManager
import ulisse.applications.useCases.RouteManager.ErrorSaving
import ulisse.entities.Route
import ulisse.infrastructures.view.components.JComponent
import ulisse.infrastructures.view.components.JStyler.*
import ulisse.infrastructures.view.form.RouteForm
import ulisse.utils.Swings.centerOf

import java.awt.Color
import scala.concurrent.ExecutionContext
import scala.swing.*
import scala.swing.BorderPanel.Position.*
import scala.swing.event.*

given ExecutionContext = ExecutionContext.fromExecutor: (runnable: Runnable) =>
  Swing.onEDT(runnable.run())

trait MapView

object MapView:
  given transparentPanel: Boolean = true

  def apply(uiPort: UIPort): MapView = MapViewImpl(uiPort)

  private case class MapViewImpl(uiPort: UIPort) extends MainFrame, MapView:

    title = "Map"
    visible = true
    preferredSize = new Dimension(800, 800)

    private val countLabel = "Count Route: "
    private val errorStr   = "Error: "

    val mapPark: MapPanel    = MapPanel.empty()
    val info: Label          = Label(s"$countLabel")
    val error: Label         = JComponent.label(errorStr, defaultStyler)
    val formPanel: RouteForm = RouteForm()

    println(s"${info.font}")

    // Main content pane with BorderLayout
    val contentPane = new BorderPanel
    val glassPane   = new BorderPanel

    val createButton = JComponent.button(
      "Form Route",
      styler(roundRect(20), hoverPalette(Color.green), defaultFont, border(Color.black, 2))
    )
    val northPanel = new FlowPanel(createButton, info, error)

    info.text = s"$countLabel 0"
    error.text = errorStr + "ddd"

    glassPane.opaque = false
    glassPane.visible = false
    formPanel.setVisible(true)

    private val dialog = new Dialog(this):
      title = "Dialogo Mobile"
      contents = formPanel.panel()
      setLocationRelativeTo(MapViewImpl.this)
      pack()
      open()
      visible = false

    // Create button action
    listenTo(createButton)
    reactions += {
      case event.ButtonClicked(_) =>
        dialog.centerOf(this)
        dialog.visible = true
    }

    // formPanel button action
    formPanel.saveButton().reactions += {
      case event.ButtonClicked(_) =>
        uiPort.save(formPanel.create()).onComplete(future =>
          for {
            either <- future
          } yield {
            either match
              case Left(errorSaving) => error.text = s"$errorStr + $errorSaving"
              case Right(routes) =>
                mapPark.setPoints(routes.map(router => router.path))
                mapPark.repaint()
                info.text = s"$countLabel ${routes.size}"
          }
        )
    }

    formPanel.exitButton().reactions += {
      case event.ButtonClicked(_) =>
//        formPanel.setVisible(false)
        dialog.close()
    }

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var field = 0

    mapPark.listenTo(mapPark.mouse.clicks)
    mapPark.reactions += {
      case event.MousePressed(_, point, _, _, _) =>
        if formPanel.visible then
          field match
            case 0 => formPanel.setDepartureStation("station", point.getX, point.getY)
            case 1 => formPanel.setArrivalStation("station", point.getX, point.getY)
          field = (field + 1) % 2
    }

    contentPane.layout(northPanel) = North
    contentPane.layout(mapPark) = Center

    contents = contentPane
    peer.setGlassPane(glassPane.peer)
    glassPane.visible = true
