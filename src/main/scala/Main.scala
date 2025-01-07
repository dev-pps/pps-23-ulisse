import cats.implicits.catsSyntaxEq

import java.awt.Color
import scala.swing.*
import scala.swing.Swing.LineBorder
import scala.swing.event.*

final case class StationCard(
    station: Station,
    openStationForm: Option[Station] => Unit
) extends GridPanel(3, 1):
  contents += new Label(s"Name: ${station.name}")
  contents += new Label(s"Location: ${station.location}")
  contents += new Label(s"Capacity: ${station.capacity}")
  border = LineBorder(Color.BLACK, 2)
  listenTo(mouse.clicks)
  reactions += {
    case MouseClicked(_, _, _, _, _) =>
      openStationForm(Option(station))
      print(s"Name: ${station.name}")
  }

final case class StationMapView(openStationForm: Option[Station] => Unit)
    extends GridPanel(5, 5):
  private val labels = for {
    x <- 0 until 5
    y <- 0 until 5
    station = model.stationMap.find(_.location === Location(x, y))
  } yield station.map(StationCard(_, openStationForm)).getOrElse(Label("Empty"))
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
  private val stationName = new TextField(5)
  private val latitude    = new TextField(5)
  private val longitude   = new TextField(5)
  private val capacity    = new TextField(5)
  for s <- station do
    stationName.text = s.name
    latitude.text = s.location.latitude.toString
    longitude.text = s.location.longitude.toString
    capacity.text = s.capacity.toString

  private def createStation(
      name: String,
      latitude: String,
      longitude: String,
      capacity: String
  ): Station =
    Station(
      name,
      Location(latitude.toDouble, longitude.toDouble),
      capacity.toInt
    )
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
  layout(capacity) = c

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
          capacity.text
        )
        for s <- station do model.removeStation(s)
        model.addStation(stationFromForm)
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
            model.removeStation(s)
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
          StationMapView(openStationForm),
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

final case class AppFrame() extends MainFrame:
  title = "Station Editor"
  minimumSize = new Dimension(400, 300)
  preferredSize = new Dimension(800, 600)
  contents = StationEditorPage()
  pack()
  centerOnScreen()

val model: Model = Model()

@main def run(): Unit =
  AppFrame().open()
