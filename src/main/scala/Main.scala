import ulisse.applications.adapters.{StationPortInputAdapter, TrainServiceManagerAdapter}
import ulisse.applications.useCases.StationManager
import ulisse.entities.Coordinates.Grid
import ulisse.infrastructures.view.AppFrame
import ulisse.infrastructures.view.adapter.StationPortOutputAdapter
import ulisse.infrastructures.view.station.{StationEditorController, StationEditorView}

import java.util.concurrent.LinkedBlockingQueue

final case class Setting():
  lazy val outputAdapter: StationPortOutputAdapter =
    StationPortOutputAdapter(stationEditorView)
  lazy val stationManager: StationManager[Int, Grid] = StationManager(outputAdapter)
  lazy val inputAdapter: StationPortInputAdapter[Int, Grid] =
    StationPortInputAdapter(stationManager)
  lazy val stationEditorController: StationEditorController = StationEditorController(inputAdapter)
  val stationEditorView: StationEditorView                  = StationEditorView(stationEditorController)

@main def main(): Unit =
  val app      = AppFrame()
  val settings = Setting()
  app.contents = settings.stationEditorView
  app.open()

@main def trainDemoMain(): Unit =
  import ulisse.applications.useCases.TrainManagers.TrainManager
  import ulisse.applications.useCases.TechnologyManagers.TechnologyManager
  import ulisse.applications.ports.TrainPorts
  import ulisse.entities.train.Trains.TrainTechnology
  import ulisse.infrastructures.view.train.TrainEditorView

  type AppState = (TrainManager, TechnologyManager[TrainTechnology])
  val stateEventQueue = LinkedBlockingQueue[AppState => AppState]

  // Train Fleet init
  val trainPort: TrainPorts.Input = TrainServiceManagerAdapter(stateEventQueue)
  val trainView                   = TrainEditorView(trainPort)
  trainView.open()

  // Init Managers state
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
