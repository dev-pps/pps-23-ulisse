package ulisse.infrastructures.view

import cats.effect.IO
import cats.effect.kernel.Ref
import cats.effect.unsafe.implicits.global
import cats.syntax.either.*
import ulisse.applications.ports.RoutePorts.UIPort
import ulisse.applications.useCases.RouteManager
import ulisse.applications.useCases.RouteManager.ErrorSaving
import ulisse.entities.Route
import ulisse.infrastructures.view.form.RouteForm

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

  private case class Points(list: List[((Int, Int), (Int, Int))])

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class MapPanel(var points: Points) extends Panel:

    override def paintComponent(g: Graphics2D): Unit = {
      super.paintComponent(g)

      points.list.foreach((p1, p2) =>
        g.setColor(java.awt.Color.GREEN)
        g.fillOval(p1._1, p1._2, 5, 5)
        g.setColor(java.awt.Color.BLACK)
        g.drawLine(p1._1, p1._2, p2._1, p2._2)
        g.setColor(java.awt.Color.GREEN)
        g.fillOval(p2._1, p2._2, 5, 5)
      )
    }

  private case class MapViewImpl(uiPort: UIPort) extends MainFrame, MapView:
    title = "Map"
    visible = true
    preferredSize = new Dimension(800, 800)

    private val countLabel = "Count Route: "
    private val errorStr   = "Error: "

    val stateStationGUI: Ref[IO, Points] = Ref.of[IO, Points](Points(List.empty)).unsafeRunSync()
    val mapPark: MapPanel                = MapPanel(Points(List.empty))
    val info: Label                      = Label(s"$countLabel")
    val error: Label                     = Label(errorStr)
    val formPanel: RouteForm             = RouteForm()

    // Main content pane with BorderLayout
    val contentPane  = new BorderPanel
    val glassPane    = new BorderPanel
    val createButton = new Button("Form Route")
    val northPanel   = new FlowPanel(createButton, info, error)

    info.text = s"$countLabel 0"
    error.text = errorStr + "ddd"

    glassPane.opaque = false
    glassPane.visible = false
    formPanel.setVisible(false)

    private val stateRef: Ref[IO, Either[ErrorSaving, UIPort]] =
      Ref.of[IO, Either[ErrorSaving, UIPort]](uiPort.asRight).unsafeRunSync()

    sealed trait Action[T]:
      def create: T
    case object RouteCreate extends Action[Option[Route]]:
      override def create: Option[Route] = formPanel.create()

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
        uiPort.save(formPanel.create()).onComplete(future =>
          for {
            either <- future
          } yield {
            either match
              case Left(errorSaving) => error.text = s"$errorStr + $errorSaving"
              case Right(routes) =>
                mapPark.points = Points(
                  routes.map(router => router.path)
                    .map(path =>
                      (
                        (path._1._2.latitude.toInt, path._1._2.longitude.toInt),
                        (path._2._2.latitude.toInt, path._2._2.longitude.toInt)
                      )
                    )
                )
                mapPark.repaint()
                info.text = s"$countLabel ${routes.size}"
          }
        )
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
          field match
            case 0 => formPanel.setDepartureStation("station", point.getX, point.getY)
            case 1 => formPanel.setArrivalStation("station", point.getX, point.getY)
          field = (field + 1) % 2

//          val update = stateStationGUI
//            .updateAndGet(state => updatePark(mapPark.points, point.x.toString, point.y.toString))
//            .flatMap(updateState =>
//              field match
//                case 0 => formPanel.setDepartureStation("station", point.getX, point.getY)
//                case 1 => formPanel.setArrivalStation("station", point.getX, point.getY)
//              field = (field + 1) % 2
//              mapPark.points = updateState
//              mapPark.repaint()
//              IO(())
//            )
//          runOnEDT(update)

    }

    contentPane.layout(northPanel) = North
    contentPane.layout(mapPark) = Center
    glassPane.layout(formPanel.panel()) = West

    contents = contentPane
    peer.setGlassPane(glassPane.peer)
