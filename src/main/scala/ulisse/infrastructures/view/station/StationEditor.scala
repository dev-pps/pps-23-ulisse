package ulisse.infrastructures.view.station

import ulisse.applications.ports.StationPorts
import ulisse.entities.Location
import ulisse.entities.Location.Grid
import ulisse.entities.station.Station

import java.awt.Color
import scala.swing.*
import scala.swing.Swing.LineBorder
import scala.swing.event.*

/** A Card displaying station information.
  *
  * @constructor
  *   Creates a new StationCard with station details.
  * @param station
  *   The associated station.
  * @param openStationForm
  *   A function that opens the station form to modify the station when the card is clicked.
  */
final case class StationCard(
    station: Station[Grid],
    openStationForm: Option[Station[Grid]] => Unit
) extends GridPanel(3, 1):
  contents += new Label(s"Name: ${station.name}")
  contents += new Label(s"Location: ${station.location}")
  contents += new Label(s"numberOfTrack: ${station.numberOfTrack}")
  border = LineBorder(Color.BLACK, 2)
  listenTo(mouse.clicks)
  reactions += {
    case MouseClicked(_, _, _, _, _) =>
      openStationForm(Option(station))
  }

/** A Card representing an empty station location.
  *
  * @constructor
  *   Creates a new EmptyMapCard.
  * @param mapLocation
  *   The location of the card.
  * @param stationForm
  *   The form where the location will be set upon card click.
  */
final case class EmptyMapCard(
    mapLocation: Grid,
    stationForm: Option[StationForm]
) extends Label:
  text = "Empty"
  border = LineBorder(Color.BLACK, 2)
  listenTo(mouse.clicks)
  reactions += {
    case MouseClicked(_, _, _, _, _) =>
      for form <- stationForm do form.setLocation(mapLocation)
  }

/** A GridPanel that displays the station map, consisting of StationCard and EmptyMapCard components.
  *
  * @constructor
  *   Creates a new StationMapView.
  * @param controller
  *   The associated StationEditorController.
  * @param openStationForm
  *   A function to open the station form when a StationCard is clicked.
  * @param stationForm
  *   The form where location details are added when an EmptyMapCard is clicked.
  */
final case class StationMapView(
    controller: StationEditorController,
    openStationForm: Option[Station[Grid]] => Unit,
    stationForm: Option[StationForm]
) extends GridPanel(5, 5):
  private val labels = for {
    x <- 0 until 5
    y <- 0 until 5
    location <- Location.createGrid(x, y) match
      case Left(value)  => None
      case Right(value) => Some(value)
    station = controller.findStationAt(location)
  } yield station.map(StationCard(_, openStationForm)).getOrElse(EmptyMapCard(location, stationForm))
  contents ++= labels

/** A GridBagPanel displaying the station editor menu.
  *
  * @constructor
  *   Creates a new StationEditorMenu.
  * @param onCreateClick
  *   A function that opens the station creation panel when the create button is clicked.
  */
final case class StationEditorMenu(onCreateClick: () => Unit)
    extends GridBagPanel:
  private val c = new Constraints
  c.anchor = GridBagPanel.Anchor.Center
  c.gridx = 0
  c.weightx = 1

  c.gridy = 0
  c.weighty = 1
  layout(Swing.VGlue) = c

  c.gridy = 1
  c.weighty = 0.1
  layout(new Label("Select a Station")) = c

  c.gridy = 2
  c.weighty = 0.1
  layout(new Label("or")) = c

  c.gridy = 3
  c.weighty = 0.1
  layout(new Button {
    text = "Create"
    reactions += {
      case ButtonClicked(_) =>
        onCreateClick()
    }
  }) = c

  c.gridy = 4
  c.weighty = 1.0
  layout(Swing.VGlue) = c

/** A GridBagPanel displaying the station editor form.
  *
  * @constructor
  *   Creates a new StationForm.
  * @param controller
  *   The associated StationEditorController.
  * @param onBackClick
  *   A function that navigates back to the station editor menu.
  * @param station
  *   The station to be edited.
  */
final case class StationForm(
    controller: StationEditorController,
    onBackClick: () => Unit,
    station: Option[Station[Grid]]
) extends GridBagPanel:
  private val stationName   = new TextField(5)
  private val latitude      = new TextField(5)
  private val longitude     = new TextField(5)
  private val numberOfTrack = new TextField(5)
  for s <- station do
    stationName.text = s.name
    latitude.text = s.location.row.toString
    longitude.text = s.location.column.toString
    numberOfTrack.text = s.numberOfTrack.toString

  /** Sets the location in the StationForm.
    *
    * This method updates the latitude and longitude fields in the form with the provided location's values.
    *
    * @param location
    *   The location to set.
    */
  def setLocation(location: Grid): Unit =
    latitude.text = location.row.toString
    longitude.text = location.column.toString

  private val c = new Constraints
  c.anchor = GridBagPanel.Anchor.Center
  c.gridx = 0
  c.weightx = 1

  c.gridwidth = 3
  c.gridy = 0
  c.weighty = 1
  layout(Swing.VGlue) = c

  c.gridy = 1
  c.weighty = 0.1
  layout(new Label("Station")) = c

  c.gridwidth = 2
  c.gridx = 0
  c.gridy = 2
  c.weighty = 0.1
  layout(new Label("Station Name: ")) = c

  c.gridx = 1
  c.weighty = 0.1
  layout(stationName) = c

  c.gridx = 0
  c.gridy = 3
  c.weighty = 0.1
  layout(new Label("Latitude: ")) = c

  c.gridx = 1
  c.weighty = 0.1
  layout(latitude) = c

  c.gridx = 0
  c.gridy = 4
  c.weighty = 0.1
  layout(new Label("Longitude: ")) = c

  c.gridx = 1
  c.weighty = 0.1
  layout(longitude) = c

  c.gridx = 0
  c.gridy = 5
  c.weighty = 0.1
  layout(new Label("Station Tracks: ")) = c

  c.gridx = 1
  c.weighty = 0.1
  layout(numberOfTrack) = c

  c.gridwidth = 1
  c.gridx = 0
  c.gridy = 6
  c.weighty = 0.1
  layout(new Button {
    text = "Ok"
    reactions += {
      case ButtonClicked(_) =>
        controller.onOkClick(
          stationName.text,
          latitude.text,
          longitude.text,
          numberOfTrack.text,
          station
        )
        onBackClick()
    }
  }) = c
  c.gridx = 1
  c.gridy = 6
  c.weighty = 0.1
  layout(new Button {
    text = "Remove"
    station match
      case Some(s) =>
        reactions += {
          case ButtonClicked(_) =>
            controller.removeStation(s)
            onBackClick()
        }
      case _ =>
        enabled = false
  }) = c
  c.gridx = 2
  c.gridy = 6
  c.weighty = 0.1
  layout(new Button {
    text = "Back"
    reactions += {
      case ButtonClicked(_) =>
        onBackClick()
    }
  }) = c
  c.gridx = 0
  c.gridy = 7
  c.weighty = 1.0
  layout(Swing.VGlue) = c

/** The content of the StationEditorView.
  *
  * @constructor
  *   Creates a new StationEditorContent.
  * @param worldMap
  *   The map displaying the stations.
  * @param stationEditorPanel
  *   The panel of the station editor menu or the station form.
  */
final case class StationEditorContent(
    worldMap: Panel,
    stationEditorPanel: Panel
)

/** The view of the StationEditor.
  *
  * @constructor
  *   Creates a new StationEditorView.
  * @param controller
  *   The associated StationEditorController.
  */
final case class StationEditorView(appPort: StationPorts.Input[Grid])
    extends BorderPanel:

  private val controller = StationEditorController(appPort)

  private val updateContentTemplate: Panel => Unit =
    (stationEditorPanel: Panel) =>
      updateContent(
        StationEditorContent(
          stationEditorPanel match
            case p: StationForm =>
              StationMapView(controller, openStationForm, Option(p))
            case _ => StationMapView(controller, openStationForm, Option.empty)
          ,
          stationEditorPanel
        )
      )

  private val openStationForm: Option[Station[Grid]] => Unit =
    station => {
      updateContentTemplate(StationForm(controller, openStationMenu, station))
    }

  private val openStationMenu: () => Unit = () =>
    updateContentTemplate(StationEditorMenu(() =>
      openStationForm(Option.empty)
    ))

  openStationMenu()

  private def makeContent(content: StationEditorContent): GridBagPanel =
    new GridBagPanel:
      private val c = new Constraints
      c.fill = GridBagPanel.Fill.Both
      c.gridx = 0
      c.gridy = 0
      c.weighty = 1
      c.weightx = 0.7
      layout(content.worldMap) = c
      c.gridx = 1
      c.weightx = 0.3
      layout(content.stationEditorPanel) = c

  private def updateContent(content: StationEditorContent): Unit =
    layout(makeContent(content)) = BorderPanel.Position.Center
    revalidate()
    repaint()
