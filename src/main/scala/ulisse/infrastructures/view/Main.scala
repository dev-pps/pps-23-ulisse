package ulisse.infrastructures.view

import ulisse.applications.AppState
import ulisse.applications.adapters.StationPortInputAdapter
import ulisse.applications.station.StationMap
import ulisse.entities.Coordinates.Grid
import ulisse.entities.station.Station
import ulisse.entities.station.Station.CheckedStation
import ulisse.infrastructures.view.AppFrame
import ulisse.infrastructures.view.StationTypes.*
import ulisse.infrastructures.view.adapter.StationPortOutputAdapter
import ulisse.infrastructures.view.menu.Menu
import ulisse.infrastructures.view.station.{StationEditorController, StationEditorView}

import java.util.concurrent.LinkedBlockingQueue

val eventStream = LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]]()

@main def launchApp(): Unit =
  val app = AppFrame()
  app.contents = Menu(app)
  app.open()

  val initialState = AppState[N, C, S](StationMap.createCheckedStationMap())
  LazyList.continually(eventStream.take()).foldLeft(initialState)((state, event) =>
    event(state)
  )

@main def stationEditor(): Unit =
  val app      = AppFrame()
  val settings = StationSettings()
  app.contents = settings.stationEditorView
  app.open()

  val initialState = AppState[N, C, S](StationMap.createCheckedStationMap())
  LazyList.continually(eventStream.take()).foldLeft(initialState)((state, event) =>
    event(state)
  )

final case class StationSettings():
  lazy val outputAdapter: StationPortOutputAdapter[N, C, S]     = StationPortOutputAdapter(stationEditorController)
  lazy val inputAdapter: StationPortInputAdapter[N, C, S]       = StationPortInputAdapter(eventStream, outputAdapter)
  val stationEditorController: StationEditorController[N, C, S] = StationEditorController(inputAdapter)
  val stationEditorView: StationEditorView                      = StationEditorView(stationEditorController)

object StationTypes:
  type N = Int
  type C = Grid
  type S = CheckedStation[N, C]

@main def trainDemoMain(): Unit =
  import ulisse.applications.adapters.TrainServiceManagerAdapter
  import ulisse.applications.ports.TrainPorts
  import ulisse.applications.useCases.TechnologyManagers.TechnologyManager
  import ulisse.applications.useCases.TrainManagers.TrainManager
  import ulisse.entities.train.Trains.TrainTechnology
  import ulisse.infrastructures.view.train.TrainEditorView

  type AppState = (TrainManager, TechnologyManager[TrainTechnology])
  val stateEventQueue = LinkedBlockingQueue[AppState => AppState]

  // Train Fleet init
  val trainPort: TrainPorts.Input = TrainServiceManagerAdapter(stateEventQueue)
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
