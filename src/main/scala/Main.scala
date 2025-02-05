import StationTypes.*
import ulisse.adapters.StationPortOutputAdapter
import ulisse.applications.AppState
import ulisse.applications.managers.StationManager
import ulisse.applications.useCases.StationPortInputService
import ulisse.entities.Coordinates.Grid
import ulisse.entities.station.Station
import ulisse.entities.station.Station.CheckedStation
import ulisse.infrastructures.view.AppFrame
import ulisse.infrastructures.view.components.{JComponent, JStyler}
import ulisse.infrastructures.view.station.{StationEditorController, StationEditorView}

import java.awt.Color
import java.util.concurrent.LinkedBlockingQueue
import scala.swing.*

@main def stationEditor(): Unit =
  val app      = AppFrame()
  val settings = StationSettings()
  app.contents = settings.stationEditorView
  app.open()

  val initialState = AppState[N, C, S](StationManager.createCheckedStationMap())
  LazyList.continually(settings.eventStream.take()).foldLeft(initialState)((state, event) =>
    event(state)
  )

final case class StationSettings():
  lazy val outputAdapter: StationPortOutputAdapter[N, C, S] = StationPortOutputAdapter(stationEditorController)
  lazy val inputAdapter: StationPortInputService[N, C, S]   = StationPortInputService(eventStream, outputAdapter)
  val eventStream = LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]]()
  val stationEditorController: StationEditorController[N, C, S] = StationEditorController(inputAdapter)
  val stationEditorView: StationEditorView                      = StationEditorView(stationEditorController)

object StationTypes:
  type N = Int
  type C = Grid
  type S = CheckedStation[N, C]

@main def trainDemoMain(): Unit =
  import ulisse.applications.managers.TechnologyManagers.TechnologyManager
  import ulisse.applications.managers.TrainManagers.TrainManager
  import ulisse.applications.ports.TrainPorts
  import ulisse.applications.useCases.TrainServiceManagerService
  import ulisse.entities.train.Trains.TrainTechnology
  import ulisse.infrastructures.view.train.TrainEditorView

  type AppState = (TrainManager, TechnologyManager[TrainTechnology])
  val stateEventQueue = LinkedBlockingQueue[AppState => AppState]

  // Train Fleet init
  val trainPort: TrainPorts.Input = TrainServiceManagerService(stateEventQueue)
  val trainView                   = TrainEditorView(trainPort)
  // trainView.open()

  // Init Managers state
  // Managers can be initialized loading entities from file/external repo
  val trainManager     = TrainManager(List.empty)
  val technologies     = List(TrainTechnology("AV", 300, 2.0, 1.0), TrainTechnology("Normal", 160, 1.0, 0.5))
  val trainTechManager = TechnologyManager[TrainTechnology](technologies)

  // App state init
  val initialState: AppState = (trainManager, trainTechManager)
  LazyList.continually(stateEventQueue.take()).scanLeft(initialState)((state, event) =>
    event(state)
  ).foreach((appState: AppState) =>
    println(s"Stations: ${appState._1.trains}")
  )

@main def testNewGraphicComponents(): Unit =
  def top: Frame = new MainFrame {
    title = "Scala Swing GUI"

    val station  = JComponent.createIconLabel("icons/station.svg", "station")
    val route    = JComponent.createIconLabel("icons/route.svg", "route")
    val schedule = JComponent.createIconLabel("icons/train.svg", "schedule")
    val tabPane  = JComponent.createTabbedPane(station, route, schedule)

    val stationNameForm      = JComponent.createInfoTextField("Name")
    val stationLatitudeForm  = JComponent.createInfoTextField("Latitude")
    val stationLongitudeForm = JComponent.createInfoTextField("Longitude")

    val routeDepartureStationForm = JComponent.createInfoTextField("Departure Station")
    val routeArrivalStationForm   = JComponent.createInfoTextField("Arrival Station")
    val routeTypeForm             = JComponent.createInfoTextField("Type")

    tabPane.paneOf(station).contents += JComponent.createBaseForm(
      "Station",
      stationNameForm,
      stationLatitudeForm,
      stationLongitudeForm
    ).component

    tabPane.paneOf(route).contents += JComponent.createBaseForm(
      "Route",
      routeDepartureStationForm,
      routeArrivalStationForm,
      routeTypeForm
    ).component

    tabPane.paneOf(schedule).contents += new Label("Simulation")

    listenTo(tabPane.component.mouse.clicks)
    // Creazione di un FlowPanel
    val northPanel = new FlowPanel() {
      contents += JComponent.createToggleIconButton("icons/train.svg", "icons/station.svg").component
    }

    // Creazione del BorderPanel
    val mainPanel = new BorderPanel {
      layout(northPanel) = BorderPanel.Position.Center
    }

    contents = mainPanel
    size = new Dimension(400, 300)
  }

  top.visible = true
//
//  val list = LinkedBlockingQueue[RouteManager => RouteManager]
//  val port = RouteUIInputService(list)
//  val map = MapView.apply(port)
