package ulisse.infrastructures.view

import ulisse.infrastructures.view.common.{FormPanel, PairPanel}

import scala.swing.*
import scala.swing.BorderPanel.Position.*
import scala.swing.event.*

trait MapView

object MapView:
  def apply(): MapView = MapViewImpl()

  private case class MapViewImpl() extends MainFrame,
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
    val typeRoute: PairPanel[Panel, Component, Component] =
      PairPanel(
        FlowPanel(),
        Label("Route Type"),
        ComboBox(Seq("Normale", "AV"))
      )

    val railsCount: PairPanel[FlowPanel, Label, TextField] =
      PairPanel(FlowPanel(), Label("Rails Count"), TextField(10))
    val departureStation: PairPanel[Panel, Component, Component] =
      PairPanel(FlowPanel(), Label("Departure Station"), TextField(10))
    val arrivalStation: PairPanel[Panel, Component, Component] =
      PairPanel(FlowPanel(), Label("Arrival Station"), TextField(10))

    val formPanel: FormPanel[BorderPanel, Component, Component] =
      FormPanel(BorderPanel(), typeRoute, railsCount, departureStation, arrivalStation)
    formPanel.setVisible(false)

    // Create button action
    listenTo(createButton)
    reactions += {
      case ButtonClicked(`createButton`) =>
        formPanel.setVisible(!formPanel.visible)
    }

//    formPanel.buttonForm().reactions += {
//      case event.ButtonClicked(_) => listener.onSubmit(formPanel.form())
//    }

    contentPane.layout(createButton) = North
    glassPane.layout(formPanel.panel()) = West

    contents = contentPane
    peer.setGlassPane(glassPane.peer)
    peer.getGlassPane.setVisible(true)
