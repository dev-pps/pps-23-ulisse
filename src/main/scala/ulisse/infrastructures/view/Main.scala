package ulisse.infrastructures.view

import ulisse.adapters.input.{SimulationPageAdapter, StationEditorAdapter}
import ulisse.adapters.output.{SimulationNotificationAdapter, SimulationNotificationAdapterRequirements}
import ulisse.applications.AppState
import ulisse.applications.managers.StationManager.CheckedStationManager
import ulisse.applications.managers.{RouteManager, SimulationManager, StationManager}
import ulisse.applications.useCases.RouteUIInputService.RouteUIInputService
import ulisse.applications.useCases.{SimulationService, StationService}
import ulisse.entities.Coordinates.Grid
import ulisse.entities.station.Station
import ulisse.entities.station.Station.CheckedStation
import ulisse.infrastructures.view.StationTypes.*
import ulisse.infrastructures.view.map.GUIView
import ulisse.infrastructures.view.menu.Menu
import ulisse.infrastructures.view.simulation.SimulationPage
import ulisse.infrastructures.view.station.StationEditorView

import java.util.concurrent.LinkedBlockingQueue

val eventStream = LinkedBlockingQueue[AppState[S] => AppState[S]]()
val updateManager = (stationManagerUpdater: CheckedStationManager[S] => CheckedStationManager[S]) =>
  eventStream.offer((state: AppState[S]) => state.copy(stationManager = stationManagerUpdater(state.stationManager)));
  ()
@main def launchApp(): Unit =
  val app = AppFrame()
  app.contents = Menu(app)
  app.open()

  val initialState = AppState[S](StationManager.createCheckedStationManager())
  LazyList.continually(eventStream.take()).foldLeft(initialState)((state, event) =>
    event(state)
  )

@main def stationEditor(): Unit =
  val app      = AppFrame()
  val settings = StationSettings()
  app.contents = settings.stationEditorView
  app.open()

  val initialState = AppState[S](StationManager.createCheckedStationManager())
  LazyList.continually(eventStream.take()).foldLeft(initialState)((state, event) =>
    event(state)
  )

final case class SimulationSettings():
  val simulationNotificationAdapter: SimulationNotificationAdapter =
    SimulationNotificationAdapter(new SimulationNotificationAdapterRequirements {
      override def simulationPageComponent: SimulationPage = simulationPage
    })
  val inputAdapter: SimulationService[S]              = SimulationService(updateManager, simulationNotificationAdapter)
  val simulationPageController: SimulationPageAdapter = SimulationPageAdapter(inputAdapter)
  val simulationPage: SimulationPage                  = SimulationPage(simulationPageController)
//  simulationNotificationAdapter.simulationPage = Some(simulationPage)

final case class StationSettings():
  lazy val inputAdapter: StationService[S]                   = StationService(eventStream)
  val stationEditorController: StationEditorAdapter[N, C, S] = StationEditorAdapter(inputAdapter)
  val stationEditorView: StationEditorView                   = StationEditorView(stationEditorController)

object StationTypes:
  type N = Int
  type C = Grid
  type S = CheckedStation[C]

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
  val list = LinkedBlockingQueue[RouteManager => RouteManager]
  val port = RouteUIInputService(list)
  val map  = GUIView(port)
