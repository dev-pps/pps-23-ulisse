package view

import scala.swing.{
  BorderPanel,
  BoxPanel,
  Button,
  ComboBox,
  Component,
  Dimension,
  FlowPanel,
  Label,
  MainFrame,
  Orientation,
  Panel,
  TextField
}
import scala.swing.BorderPanel.Position.*
import scala.swing.event.*

trait MapView

object MapView:
  def apply(): MapView = MapViewImpl()

  private case class MapViewImpl() extends MainFrame, MapView:
    title = "Map"
    visible = true
    preferredSize = new Dimension(800, 800)

    // Main content pane with BorderLayout
    val contentPane  = new BorderPanel
    val glassPane    = new BorderPanel
    val createButton = new Button("Create")

    glassPane.visible = true
    glassPane.opaque = false

    given transparentPanel: Boolean = false

    // Panel to appear on glassPane with form fields
    val typeRoute: PairPanel[Panel, Component, Component] =
      PairPanel(
        WrapPanel.flow,
        Label("Route Type"),
        ComboBox(Seq("Normale", "AV"))
      )
    val railsCount: PairPanel[Panel, Component, Component] =
      PairPanel(WrapPanel.flow, Label("Rails Count"), TextField())
    val departureStation: PairPanel[Panel, Component, Component] =
      PairPanel(WrapPanel.flow, Label("Departure Station"), TextField())
    val arrivalStation: PairPanel[Panel, Component, Component] =
      PairPanel(WrapPanel.flow, Label("Arrival Station"), TextField())

    val formPanel: FormPanel[Panel, Panel, Component, Component] = FormPanel(
      WrapPanel.box(Orientation.Vertical),
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
        formPanel.setVisible(true)
    }

    contentPane.layout(createButton) = North
    glassPane.layout(formPanel.panel) = West

    contents = contentPane
    peer.setGlassPane(glassPane.peer)
    peer.getGlassPane.setVisible(true)
