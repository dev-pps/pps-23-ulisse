package ulisse.infrastructures.view

import ulisse.applications.ports.RoutePorts.UIPort
import ulisse.infrastructures.view.common.FormPanel

import scala.swing.*
import scala.swing.BorderPanel.Position.*
import scala.swing.event.*

trait MapView

object MapView:
  given transparentPanel: Boolean = true

  def apply(uiPort: UIPort): MapView =
    MapViewImpl(uiPort, MapPark(List().empty), Label(""), Label(""), FormPanel.route)

  private case class MapPark(list: List[(String, String)]) extends Panel:
    override def paintComponent(g: Graphics2D): Unit = {
      super.paintComponent(g)
      g.setColor(java.awt.Color.BLACK)
      list.foreach((x, y) => g.drawOval(x.toInt, y.toInt, 10, 10))
    }

  private case class MapViewImpl(
      uiPort: UIPort,
      mapPark: MapPark,
      info: Label,
      error: Label,
      formPanel: FormPanel[BorderPanel]
  ) extends MainFrame,
        MapView:
    title = "Map"
    visible = true
    preferredSize = new Dimension(800, 800)

    // Main content pane with BorderLayout
    val contentPane  = new BorderPanel
    val glassPane    = new BorderPanel
    val createButton = new Button("Create")
    val northPanel   = new FlowPanel(createButton, info, error)

    private val countLabel = "Count Route: "
    private val errorStr   = "Error: "

    info.text = countLabel + uiPort.size.toString
    error.text = errorStr + "None"

    glassPane.visible = formPanel.visible
    glassPane.opaque = false

    // Panel to appear on glassPane with form fields
//    private val formPanel: formPanel.setVisible(false)

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

    val departureStation = formPanel.keyValuesPanel(1)
    val arrivalStation   = formPanel.keyValuesPanel(2)

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var field = departureStation

    mapPark.listenTo(mapPark.mouse.clicks)
    mapPark.reactions += {
      case event.MousePressed(_, point, _, _, _) =>
        if glassPane.visible then
          val departure = field.values[TextField]
          val name      = departure(0)
          val x         = departure(1)
          val y         = departure(2)

          name.text = "Station"
          x.text = point.x.toString
          y.text = point.y.toString

          mapPark.list match
            case list if list.size < 2 =>
              copy(
                formPanel = formPanel,
                mapPark = MapPark(list :+ (point.x.toString, point.y.toString))
              )
            case list if list.size % 2 == 0 =>
              copy(
                formPanel = formPanel,
                mapPark = MapPark(list.updated(1, (point.x.toString, point.y.toString)))
              )
            case list if list.size % 2 == 1 =>
              copy(
                formPanel = formPanel,
                mapPark = MapPark(list.updated(1, (point.x.toString, point.y.toString)))
              )

          println(s"Mouse clicked at $point size: ${mapPark.list.size}")
          mapPark.repaint()
          field = if field equals departureStation then arrivalStation else departureStation
    }

    contentPane.layout(northPanel) = North
    contentPane.layout(mapPark) = Center
    glassPane.layout(formPanel.panel()) = West

    contents = contentPane
    peer.setGlassPane(glassPane.peer)
