package view

import scala.swing.{
  BorderPanel,
  Button,
  ComboBox,
  Component,
  Dimension,
  Label,
  MainFrame,
  Orientation,
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

    // Panel to appear on glassPane with form fields
    val typeRoute: PairPanel[Component, Component] =
      PairPanel(Label("Route Type"), ComboBox(Seq("Normale", "AV")))
    val railsCount: PairPanel[Component, Component] =
      PairPanel(Label("Rails Count"), TextField())
    val departureStation: PairPanel[Component, Component] =
      PairPanel(Label("Departure Station"), TextField())
    val arrivalStation: PairPanel[Component, Component] =
      PairPanel(Label("Arrival Station"), TextField())

    val formPanel: FormPanel[_] = FormPanel(Orientation.Vertical)(List(
      typeRoute,
      railsCount,
      departureStation,
      arrivalStation
    ))
    formPanel.visible = false

    // Create button action
    listenTo(createButton)
    reactions += {
      case ButtonClicked(`createButton`) =>
        formPanel.visible = true
    }

    contentPane.layout(createButton) = North
    glassPane.layout(formPanel) = West

    contents = contentPane
    peer.setGlassPane(glassPane.peer)
    peer.getGlassPane.setVisible(true)
