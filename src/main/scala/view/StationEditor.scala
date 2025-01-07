package view

import cats.implicits.catsSyntaxEq

import java.awt.Color
import scala.swing.*
import scala.swing.Swing.LineBorder
import scala.swing.event.*
import model.Model
import model.station.*
import model.station.Location.Location

val appModel: Model = Model()

final case class StationCard(
    station: Station,
    openStationForm: Option[Station] => Unit
) extends GridPanel(3, 1):
  contents += new Label(s"Name: ${station.name}")
  contents += new Label(s"Location: ${station.location}")
  contents += new Label(s"numberOfTrack: ${station.numberOfTrack}")
  border = LineBorder(Color.BLACK, 2)
  listenTo(mouse.clicks)
  reactions += {
    case MouseClicked(_, _, _, _, _) =>
      openStationForm(Option(station))
      print(s"Name: ${station.name}")
  }

final case class EmptyMapCard(
    stationLocation: Location,
    stationForm: Option[StationForm]
) extends Label:
  text = "Empty"
  border = LineBorder(Color.BLACK, 2)
  listenTo(mouse.clicks)
  reactions += {
    case MouseClicked(_, _, _, _, _) =>
      for form <- stationForm do form.setLocation(stationLocation)
  }

final case class StationMapView(
    openStationForm: Option[Station] => Unit,
    stationForm: Option[StationForm]
) extends GridPanel(5, 5):
  private val labels = for {
    x <- 0 until 5
    y <- 0 until 5
    station = appModel.stationMap.find(_.location === Location(x, y))
  } yield station.map(StationCard(_, openStationForm)).getOrElse(EmptyMapCard(Location(x, y), stationForm))
  contents ++= labels

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

final case class StationForm(
    onBackClick: () => Unit,
    station: Option[Station]
) extends GridBagPanel:
  private val stationName   = new TextField(5)
  private val latitude      = new TextField(5)
  private val longitude     = new TextField(5)
  private val numberOfTrack = new TextField(5)
  for s <- station do
    stationName.text = s.name
    latitude.text = s.location.latitude.toString
    longitude.text = s.location.longitude.toString
    numberOfTrack.text = s.numberOfTrack.toString

  private def createStation(
      name: String,
      latitude: String,
      longitude: String,
      numberOfTrack: String
  ): Station =
    Station(
      name,
      Location(latitude.toDouble, longitude.toDouble),
      numberOfTrack.toInt
    )

  def setLocation(location: Location): Unit =
    this.latitude.text = location.latitude.toString
    this.longitude.text = location.longitude.toString

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
        val stationFromForm: Station = createStation(
          stationName.text,
          latitude.text,
          longitude.text,
          numberOfTrack.text
        )
        for s <- station do appModel.removeStation(s)
        appModel.addStation(stationFromForm)
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
            appModel.removeStation(s)
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

final case class StationEditorContent(
    worldMap: Panel,
    stationEditorPanel: Panel
)

final case class StationEditorPage()
    extends BorderPanel:

  private val updateContentTemplate: Panel => Unit =
    (stationEditorPanel: Panel) =>
      updateContent(
        StationEditorContent(
          stationEditorPanel match
            case p: StationForm => StationMapView(openStationForm, Option(p))
            case _              => StationMapView(openStationForm, Option.empty)
          ,
          stationEditorPanel
        )
      )

  private val openStationForm: Option[Station] => Unit =
    station =>
      updateContentTemplate(
        StationForm(openStationMenu, station)
      )

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
