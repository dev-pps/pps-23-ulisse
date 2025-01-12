package infrastructures.ui

import infrastructures.ui.ports.ListenerOutPort
import scala.swing.BorderPanel.Position.*
import scala.swing.event.*
import scala.swing.*

trait MapView

object MapView:
  def apply(listener: ListenerOutPort): MapView = MapViewImpl(listener)

  private case class MapViewImpl(listener: ListenerOutPort) extends MainFrame,
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
        WrapPanel.flow,
        Label("Route Type"),
        ComboBox(Seq("Normale", "AV"))
      )
    val railsCount: PairPanel[Panel, Component, Component] =
      PairPanel(WrapPanel.flow, Label("Rails Count"), TextField(10))
    val departureStation: PairPanel[Panel, Component, Component] =
      PairPanel(WrapPanel.flow, Label("Departure Station"), TextField(10))
    val arrivalStation: PairPanel[Panel, Component, Component] =
      PairPanel(WrapPanel.flow, Label("Arrival Station"), TextField(10))

    val formPanel: FormPanel[Panel, Panel, Component, Component] = FormPanel(
      WrapPanel.border,
      List(
        typeRoute,
        railsCount,
        departureStation,
        arrivalStation
      )
    )
    formPanel.setVisible(false)

    // Create button action
    listenTo(createButton)
    reactions += {
      case ButtonClicked(`createButton`) =>
        formPanel.setVisible(!formPanel.visible)
    }

    formPanel.buttonForm().reactions += {
      case event.ButtonClicked(_) => listener.onSubmit(formPanel.form())
    }

    contentPane.layout(createButton) = North
    glassPane.layout(formPanel.panel) = West

    contents = contentPane
    peer.setGlassPane(glassPane.peer)
    peer.getGlassPane.setVisible(true)
