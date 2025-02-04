import StationTypes.*
import ulisse.adapters.StationPortOutputAdapter
import ulisse.applications.AppState
import ulisse.applications.managers.StationManager
import ulisse.applications.useCases.StationPortInputService
import ulisse.entities.Coordinates.Grid
import ulisse.entities.station.Station
import ulisse.entities.station.Station.CheckedStation
import ulisse.infrastructures.view.AppFrame
import ulisse.infrastructures.view.station.{StationEditorController, StationEditorView}

import java.util.concurrent.LinkedBlockingQueue

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
  import ulisse.applications.managers.TrainManagers.TrainManager
  import ulisse.applications.managers.TechnologyManagers.TechnologyManager
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
