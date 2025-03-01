package ulisse.infrastructures.view

import ulisse.adapters.input.{SimulationPageAdapter, StationEditorAdapter}
import ulisse.adapters.output.SimulationNotificationAdapter
import ulisse.adapters.output.UtilityAdapters.TimeProviderAdapter
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.useCases.{RouteService, SimulationService, StationService}
import ulisse.applications.{AppState, EventQueue}
import ulisse.entities.train.Trains.TrainTechnology
import ulisse.infrastructures.commons.TimeProviders.TimeProvider
import ulisse.infrastructures.utilty.{SimulationNotificationAdapterRequirements, SimulationNotificationBridge}
import ulisse.infrastructures.view.simulation.SimulationPage
import ulisse.infrastructures.view.station.StationEditorView

import java.util.concurrent.LinkedBlockingQueue

val eventQueue = EventQueue()

def runEngine(): Unit =
  val timeProviderAdapter = TimeProviderAdapter(TimeProvider.systemTimeProvider())
  val technologies        = List(TrainTechnology("AV", 300, 2.0, 1.0), TrainTechnology("Normal", 160, 1.0, 0.5))
  val initialState        = AppState()
  eventQueue.startProcessing(initialState)

@main def launchApp(): Unit =
  val app = AppFrame()
//  app.contents = Menu(app)
//  app.open()

  runEngine()

@main def stationEditor(): Unit =
  val app      = AppFrame()
  val settings = StationSettings()
  app.contents = settings.stationEditorView
  app.open()
  runEngine()

final case class SimulationSettings():
  val simulationNoficationBridge = SimulationNotificationBridge(() => simulationPage)

  val simulationNotificationAdapter: SimulationNotificationAdapter =
    SimulationNotificationAdapter(simulationNoficationBridge)

  val inputAdapter: SimulationService = SimulationService(eventQueue, simulationNotificationAdapter)

  val simulationPageController: SimulationPageAdapter = SimulationPageAdapter(inputAdapter)
  val simulationPage: SimulationPage                  = SimulationPage(simulationPageController)

final case class StationSettings():
  val inputAdapter: StationService                  = StationService(eventQueue)
  val stationEditorController: StationEditorAdapter = StationEditorAdapter(inputAdapter)
  val stationEditorView: StationEditorView          = StationEditorView(stationEditorController)

@main def trainDemoMain(): Unit =
  import ulisse.applications.managers.TechnologyManagers.TechnologyManager
  import ulisse.applications.managers.TrainManagers.TrainManager
  import ulisse.applications.ports.TrainPorts
  import ulisse.applications.useCases.TrainService
  import ulisse.entities.train.Trains.TrainTechnology
  import ulisse.infrastructures.view.train.TrainEditorView

  type AppState = (TrainManager, TechnologyManager[TrainTechnology])
  val stateEventQueue = LinkedBlockingQueue[AppState => AppState]

  // Train Fleet init
  val trainPort: TrainPorts.Input = TrainService(stateEventQueue)
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
  val list = LinkedBlockingQueue[RouteManager => RouteManager]()
  val port = RouteService(list)
  val map  = GUIView(port)
