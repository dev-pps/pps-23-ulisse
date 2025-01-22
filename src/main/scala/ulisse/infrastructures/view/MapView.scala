package ulisse.infrastructures.view

import cats.effect.IO
import cats.effect.kernel.Ref
import cats.effect.unsafe.implicits.global
import ulisse.applications.ports.RoutePorts.UIPort
import ulisse.applications.useCases.RouteManager
import ulisse.entities.Route
import ulisse.infrastructures.view.common.FormPanel

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

    val stateStationGUI: Ref[IO, Points]  = Ref.of[IO, Points](Points(List.empty)).unsafeRunSync()
    val mapPark: MapPanel                 = MapPanel(Points(List.empty))
    val info: Label                       = Label(s"$countLabel ${uiPort.size}")
    val error: Label                      = Label(errorStr)
    val formPanel: FormPanel[BorderPanel] = FormPanel.route

    // Main content pane with BorderLayout
    val contentPane  = new BorderPanel
    val glassPane    = new BorderPanel
    val createButton = new Button("Create")
    val northPanel   = new FlowPanel(createButton, info, error)

    info.text = countLabel + uiPort.size.toString
    error.text = errorStr + "None"

    glassPane.visible = formPanel.visible
    glassPane.opaque = false

    // Panel to appear on glassPane with form fields
//    private val formPanel: formPanel.setVisible(false)

    sealed trait Action[T]:
      def create: T

    case object RouteCreate extends Action[Option[Route]]:
      override def create: Option[Route] = formPanel.create()

//    case object DepartureStation extends Action[Route.Station]:
//      private val departureStation = formPanel.keyValuesPanel(1)
//      private val values           = departureStation.values[TextField]
//      private val name             = values(1).text
//      val coordinate               = Coordinate(values(2).text.toDouble, values(3).text.toDouble)
//      override def create: Station = (name, coordinate)
//
//    case object ArrivalStation extends Action[Station]:
//      private val arrivalStation   = formPanel.keyValuesPanel(2)
//      private val values           = arrivalStation.values[TextField]
//      private val name             = values(1).text
//      val coordinate               = Coordinate(values(2).text.toDouble, values(3).text.toDouble)
//      override def create: Station = (name, coordinate)
//
//    case object Length extends Action[Double]:
//      private val length          = formPanel.keyValuesPanel(3)
//      private val value           = length.values[TextField]
//      override def create: Double = value(1).text.toDouble
//
//    case object CountRails extends Action[Int]:
//      private val railsCount   = formPanel.keyValuesPanel(4)
//      private val value        = railsCount.values[TextField]
//      override def create: Int = value(1).text.toInt

    // Funzione di aggiornamento dello stato
    def updateState(state: UIPort, route: Route): Either[RouteManager.ErrorSaving, UIPort] =
      state.save(route)

    val stateRef: Ref[IO, UIPort] = Ref.of[IO, UIPort](uiPort).unsafeRunSync()

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
        for {
          route <- formPanel.create()
        } yield {
          formPanel.setVisible(false)
          glassPane.visible = false

          uiPort.save(route) match
            case Left(errorSaving) =>
              println("ERROR")
              error.text = errorStr + errorSaving.toString
            case Right(port) =>
              println(s"CREATED ${port.size}")
              info.text = countLabel + port.size.toString
              repaint()
              validate()
              this.copy(uiPort = port)
        }
    }

    formPanel.exitButton().reactions += {
      case event.ButtonClicked(_) => formPanel.setVisible(false)
    }

    def updatePark(points: Points, x: String, y: String): Points =
      points.copy(list = points.list :+ (x, y))

    def runOnEDT(io: IO[Unit]): Unit = Swing.onEDT(io.unsafeRunAndForget())

    //    val departureStation = formPanel.keyValuesPanel(1)
//    val arrivalStation   = formPanel.keyValuesPanel(2)

//    @SuppressWarnings(Array("org.wartremover.warts.Var"))
//    private var field = departureStation

    mapPark.listenTo(mapPark.mouse.clicks)
    mapPark.reactions += {
      case event.MousePressed(_, point, _, _, _) =>
        if glassPane.visible then

          val update = stateStationGUI
            .updateAndGet(state => updatePark(mapPark.points, point.x.toString, point.y.toString))
            .flatMap(updateState =>
//              mapPark.stateRef.updateAndGet(updateState)
              formPanel
              mapPark.points = updateState
              mapPark.repaint()
              IO(println(s"STATE_GUI: ${updateState.list}"))
            )
          runOnEDT(update)

//          val departure = field.values[TextField]
//          val name      = departure(0)
//          val x         = departure(1)
//          val y         = departure(2)
//
//          name.text = "Station"
//          x.text = point.x.toString
//          y.text = point.y.toString
//
//          mapPark.list match
//            case list if list.size < 2 =>
//              copy(
//                formPanel = formPanel,
//                mapPark = MapPark(list :+ (point.x.toString, point.y.toString))
//              )
//            case list if list.size % 2 == 0 =>
//              copy(
//                formPanel = formPanel,
//                mapPark = MapPark(list.updated(1, (point.x.toString, point.y.toString)))
//              )
//            case list if list.size % 2 == 1 =>
//              copy(
//                formPanel = formPanel,
//                mapPark = MapPark(list.updated(1, (point.x.toString, point.y.toString)))
//              )
//
//          println(s"Mouse clicked at $point size: ${mapPark.list.size}")
//          mapPark.repaint()
//          field = if field equals departureStation then arrivalStation else departureStation
    }

    contentPane.layout(northPanel) = North
    contentPane.layout(mapPark) = Center
    glassPane.layout(formPanel.panel()) = West

    contents = contentPane
    peer.setGlassPane(glassPane.peer)
