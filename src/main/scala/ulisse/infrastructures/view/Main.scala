package ulisse.infrastructures.view

import ulisse.adapters.input.{SimulationPageAdapter, StationEditorAdapter}
import ulisse.adapters.output.UtilityAdapters.TimeProviderAdapter
import ulisse.adapters.output.SimulationNotificationAdapter
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.applications.useCases.{RouteService, SimulationService, StationService}
import ulisse.applications.{AppState, SimulationState}
import ulisse.entities.Coordinate
import ulisse.entities.station.Station
import ulisse.infrastructures.commons.TimeProviders.TimeProvider
import ulisse.infrastructures.utilty.{SimulationNotificationAdapterRequirements, SimulationNotificationBridge}
import ulisse.infrastructures.view.simulation.SimulationPage
import ulisse.infrastructures.view.station.StationEditorView

import java.util.concurrent.{Executors, LinkedBlockingQueue}

val eventStream           = LinkedBlockingQueue[AppState => AppState]()
val simulationEventStream = LinkedBlockingQueue[SimulationState => SimulationState]()

def runEngines(): Unit =
  val simulationInitialState =
    SimulationState(SimulationManager.emptyBatchManager(TimeProviderAdapter(TimeProvider.systemTimeProvider())))
  Executors.newSingleThreadExecutor().execute(() =>
    LazyList.continually(simulationEventStream.take()).foldLeft(simulationInitialState)((state, event) =>
      event(state)
    )
  )
  val initialState = AppState(StationManager())
  LazyList.continually(eventStream.take()).foldLeft(initialState)((state, event) =>
    event(state)
  )

@main def launchApp(): Unit =
  val app = AppFrame()
//  app.contents = Menu(app)
//  app.open()

  runEngines()

@main def stationEditor(): Unit =
  val app      = AppFrame()
  val settings = StationSettings()
  app.contents = settings.stationEditorView
  app.open()
  runEngines()

final case class SimulationSettings():
  val simulationNoficationBridge = SimulationNotificationBridge(() => simulationPage)

  val simulationNotificationAdapter: SimulationNotificationAdapter =
    SimulationNotificationAdapter(simulationNoficationBridge)

  val inputAdapter: SimulationService = SimulationService(
    eventStream,
    simulationEventStream,
    simulationNotificationAdapter
  )

  val simulationPageController: SimulationPageAdapter = SimulationPageAdapter(inputAdapter)
  val simulationPage: SimulationPage                  = SimulationPage(simulationPageController)

final case class StationSettings():
  val inputAdapter: StationService                  = StationService(eventStream)
  val stationEditorController: StationEditorAdapter = StationEditorAdapter(inputAdapter)
  val stationEditorView: StationEditorView          = StationEditorView(stationEditorController)

@main def trainDemoMain(): Unit =
  import ulisse.applications.managers.TechnologyManagers.TechnologyManager
  import ulisse.applications.managers.TrainManagers.TrainManager
  import ulisse.applications.ports.TrainPorts
  import ulisse.applications.useCases.TrainService
  import ulisse.entities.train.Trains.TrainTechnology
  import ulisse.infrastructures.view.train.TrainEditorView
  import ulisse.infrastructures.view.utils.SwingUtils.showPreview

  type AppState = (TrainManager, TechnologyManager[TrainTechnology])
  val stateEventQueue = LinkedBlockingQueue[AppState => AppState]

  // Train Fleet init
  val trainPort: TrainPorts.Input = TrainService(stateEventQueue)
  val trainView                   = TrainEditorView(trainPort)
  trainView.showPreview()

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
  val list = LinkedBlockingQueue[RouteManager => RouteManager]()
  val port = RouteService(list)
  val map  = GUIView(port)
