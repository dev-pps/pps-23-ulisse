package ulisse.infrastructures.view

import cats.implicits.catsSyntaxEq
import ulisse.applications.ports.RoutePorts.UIPort
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Route
import ulisse.entities.Route.TypeRoute.Normal
import ulisse.infrastructures.view.common.{FormPanel, KeyValuesPanel, PairPanel}

import scala.collection.immutable.{AbstractSeq, LinearSeq}
import scala.collection.mutable
import scala.swing.*
import scala.swing.BorderPanel.Position.*
import scala.swing.event.*

trait MapView

object MapView:
  def apply(uiPort: UIPort): MapView = MapViewImpl(uiPort)

  private case class MapPark() extends Panel:
    // unsuppresion of unchecked warning
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    var list: List[(String, String)] = List[(String, String)]().empty

    override def paintComponent(g: Graphics2D): Unit = {
      super.paintComponent(g)
      g.setColor(java.awt.Color.BLACK)
      list.foreach((x, y) => g.drawOval(x.toInt, y.toInt, 10, 10))
    }

  private case class MapViewImpl(uiPort: UIPort) extends MainFrame,
        MapView:
    title = "Map"
    visible = true
    preferredSize = new Dimension(800, 800)

    // Main content pane with BorderLayout
    val contentPane = new BorderPanel
    val glassPane   = new BorderPanel

    val mapPanel     = MapPark()
    val createButton = new Button("Create")

    contentPane.visible = true
    glassPane.visible = true
    glassPane.opaque = false

    given transparentPanel: Boolean = true

    // Panel to appear on glassPane with form fields
    private val typeRoute = KeyValuesPanel(FlowPanel())(Label("Route Type"), ComboBox(Seq("Normale", "AV")))
    private val departureStation =
      KeyValuesPanel(FlowPanel())(Label("Departure Station"), TextField(5), TextField(3), TextField(3))
    private val arrivalStation =
      KeyValuesPanel(FlowPanel())(Label("Arrival Station"), TextField(5), TextField(3), TextField(3))
    private val railsCount = KeyValuesPanel(FlowPanel())(Label("Rails Count"), TextField(10))
    private val length     = KeyValuesPanel(FlowPanel())(Label("Length"), TextField(10))

    private val formPanel: FormPanel[BorderPanel] =
      FormPanel(BorderPanel(), typeRoute, departureStation, arrivalStation, length, railsCount)
    formPanel.setVisible(false)

    // Create button action
    listenTo(createButton)
    reactions += {
      case ButtonClicked(`createButton`) =>
        glassPane.visible = true
        formPanel.setVisible(true)
    }

    // formPanel button action
    formPanel.saveButton().reactions += {
      case event.ButtonClicked(_) =>
        formPanel.create().map(uiPort.save)
    }

    formPanel.exitButton().reactions += {
      case event.ButtonClicked(_) => formPanel.setVisible(false)
    }

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var field: KeyValuesPanel[FlowPanel] = departureStation

    mapPanel.listenTo(mapPanel.mouse.clicks)
    mapPanel.reactions += {
      case event.MousePressed(_, point, _, _, _) =>
        if glassPane.visible then
          val departure = field.values[TextField]
          val name      = departure(0)
          val x         = departure(1)
          val y         = departure(2)

          name.text = "Station"
          x.text = point.x.toString
          y.text = point.y.toString

          mapPanel.list.size match
            case size if size < 2 => mapPanel.list = mapPanel.list.appended(point.x.toString, point.y.toString)
            case size if size % 2 == 0 => mapPanel.list = mapPanel.list.updated(0, (point.x.toString, point.y.toString))
            case size if size % 2 == 1 => mapPanel.list = mapPanel.list.updated(1, (point.x.toString, point.y.toString))

          println(s"Mouse clicked at $point size: ${mapPanel.list.size}")
          mapPanel.repaint()
          field = if field equals departureStation then arrivalStation else departureStation
    }

    contentPane.layout(createButton) = North
    contentPane.layout(mapPanel) = Center
    glassPane.layout(formPanel.panel()) = West

    contents = contentPane
    peer.setGlassPane(glassPane.peer)
