package ulisse.infrastructures.view

import ulisse.applications.ports.RoutePorts.UIPort
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Route
import ulisse.entities.Route.TypeRoute.Normal
import ulisse.infrastructures.view.common.{FormPanel, PairPanel}

import scala.swing.*
import scala.swing.BorderPanel.Position.*
import scala.swing.event.*

trait MapView

object MapView:
  def apply(uiPort: UIPort): MapView = MapViewImpl(uiPort)

  private case class MapViewImpl(uiPort: UIPort) extends MainFrame,
        MapView:
    title = "Map"
    visible = true
    preferredSize = new Dimension(800, 800)

    // Main content pane with BorderLayout
    val contentPane  = new BorderPanel
    val glassPane    = new BorderPanel
    val createButton = new Button("Create")

    glassPane.visible = true
    glassPane.opaque = false

    given transparentPanel: Boolean = true

    // Panel to appear on glassPane with form fields
    private val typeRoute: PairPanel[Panel, Component, Component] =
      PairPanel(FlowPanel(), Label("Route Type"), ComboBox(Seq("Normale", "AV")))
    private val railsCount: PairPanel[FlowPanel, Label, TextField] =
      PairPanel(FlowPanel(), Label("Rails Count"), TextField(10))
    private val departureStation: PairPanel[Panel, Component, Component] =
      PairPanel(FlowPanel(), Label("Departure Station"), TextField(10))
    private val arrivalStation: PairPanel[Panel, Component, Component] =
      PairPanel(FlowPanel(), Label("Arrival Station"), TextField(10))

    private val formPanel: FormPanel[BorderPanel, Component, Component] =
      FormPanel(BorderPanel(), typeRoute, railsCount, departureStation, arrivalStation)
    formPanel.setVisible(false)

    // Create button action
    listenTo(createButton)
    reactions += {
      case ButtonClicked(`createButton`) => formPanel.setVisible(true)
    }

    // formPanel button action
    formPanel.saveButton().reactions += {
      case event.ButtonClicked(_) =>
        val route =
          Route(Normal, (("Rimini", Coordinate(10.0d, 10.0d)), ("Cesena", Coordinate(20.0d, 20.0d))), 30.0d, 2)
        uiPort.save(route)
    }

    formPanel.exitButton().reactions += {
      case event.ButtonClicked(_) => formPanel.setVisible(false)
    }

    contentPane.layout(createButton) = North
    glassPane.layout(formPanel.panel()) = West

    contents = contentPane
    peer.setGlassPane(glassPane.peer)
    peer.getGlassPane.setVisible(true)
