package ulisse.infrastructures.view

import cats.effect.IO
import cats.effect.kernel.Ref
import cats.effect.unsafe.implicits.global
import cats.syntax.either.*
import ulisse.applications.ports.RoutePorts.UIPort
import ulisse.applications.useCases.RouteManager
import ulisse.entities.Route
import ulisse.infrastructures.view.form.RouteForm

import scala.swing.*
import scala.swing.BorderPanel.Position.*
import scala.swing.event.*

trait MapView

object MapView:
  given transparentPanel: Boolean = true

  def apply(uiPort: UIPort): MapView = MapViewImpl(uiPort)

  private case class Points(list: List[(String, String)])

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class MapPanel(var points: Points) extends Panel:
//    val stateRef: Ref[IO, Points] = Ref.of[IO, Points](Points(List.empty)).unsafeRunSync()

    def add(x: String, y: String): Points =
      points.copy(list = points.list :+ (x, y))

    override def paintComponent(g: Graphics2D): Unit = {
      super.paintComponent(g)
      g.setColor(java.awt.Color.BLACK)
      points.list.foreach((x, y) => g.drawOval(x.toInt, y.toInt, 10, 10))
    }

  private case class MapViewImpl(uiPort: UIPort) extends MainFrame,
        MapView:
    title = "Map"
    visible = true
    preferredSize = new Dimension(800, 800)

    private val countLabel = "Count Route: "
    private val errorStr   = "Error: "

    val stateStationGUI: Ref[IO, Points] = Ref.of[IO, Points](Points(List.empty)).unsafeRunSync()
    val mapPark: MapPanel                = MapPanel(Points(List.empty))
    val info: Label                      = Label(s"$countLabel ${uiPort.size}")
    val error: Label                     = Label(errorStr)
    val formPanel: RouteForm             = RouteForm()

    // Main content pane with BorderLayout
    val contentPane  = new BorderPanel
    val glassPane    = new BorderPanel
    val createButton = new Button("Form Route")
    val northPanel   = new FlowPanel(createButton, info, error)

    info.text = countLabel + uiPort.size.toString
    error.text = errorStr + "ddd"

    glassPane.opaque = false
    glassPane.visible = false
    formPanel.setVisible(false)

    private val stateRef: Ref[IO, Either[RouteManager.ErrorSaving, UIPort]] =
      Ref.of[IO, Either[RouteManager.ErrorSaving, UIPort]](uiPort.asRight).unsafeRunSync()

    sealed trait Action[T]:
      def create: T
    case object RouteCreate extends Action[Option[Route]]:
      override def create: Option[Route] = formPanel.create()

    // Funzione di aggiornamento dello stato
    private def updateState(
        state: Either[RouteManager.ErrorSaving, UIPort],
        route: Option[Route]
    ): Either[RouteManager.ErrorSaving, UIPort] =
      state.flatMap(uiPort => uiPort.save(route))

    private def updatePark(points: Points, x: String, y: String): Points =
      points.copy(list = points.list :+ (x, y))

    private def runOnEDT(io: IO[Unit]): Unit = Swing.onEDT(io.unsafeRunAndForget())

    // Create button action
    listenTo(createButton)
    reactions += {
      case event.ButtonClicked(_) =>
        glassPane.visible = true
        formPanel.setVisible(true)
    }

    // formPanel button action
    formPanel.saveButton().reactions += {
      case event.ButtonClicked(_) =>
        val update = stateRef
          .updateAndGet(state => updateState(state, formPanel.create()))
          .flatMap {
            case Left(errorSaving) =>
              println(errorSaving)
              IO(error.text = s"$errorStr + ${errorSaving.toString}")
            case Right(port) => IO(info.text = s"$countLabel ${port.size}")
          }
        runOnEDT(update)
    }

    formPanel.exitButton().reactions += {
      case event.ButtonClicked(_) =>
        formPanel.setVisible(false)
        glassPane.visible = false
    }

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var field = 0

    mapPark.listenTo(mapPark.mouse.clicks)
    mapPark.reactions += {
      case event.MousePressed(_, point, _, _, _) =>
        if formPanel.visible then
          val update = stateStationGUI
            .updateAndGet(state => updatePark(mapPark.points, point.x.toString, point.y.toString))
            .flatMap(updateState =>
              field match
                case 0 => formPanel.setDepartureStation("station", point.getX, point.getY)
                case 1 => formPanel.setArrivalStation("station", point.getX, point.getY)
              field = (field + 1) % 2
              mapPark.points = updateState
              mapPark.repaint()
              IO(())
            )
          runOnEDT(update)

    }

    contentPane.layout(northPanel) = North
    contentPane.layout(mapPark) = Center
    glassPane.layout(formPanel.panel()) = West

    contents = contentPane
    peer.setGlassPane(glassPane.peer)
