import ulisse.applications.AppState
import ulisse.applications.adapters.StationPortInputAdapter
import ulisse.entities.Coordinates.{Coordinate, Grid}
import ulisse.entities.station.Station
import ulisse.entities.station.Station.{createCheckedStation, CheckedStation}
import ulisse.infrastructures.view.AppFrame
import ulisse.infrastructures.view.adapter.StationPortOutputAdapter
import ulisse.infrastructures.view.station.{StationEditorController, StationEditorView}
import ulisse.utils.Errors.BaseError

import java.util.concurrent.LinkedBlockingQueue
import StationTypes.*
import ulisse.applications.station.StationMap

object StationTypes:
  type N = Int
  type C = Grid
  type S = CheckedStation[N, C]

final case class StationSettings():
  val eventStream = LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]]()
  lazy val outputAdapter: StationPortOutputAdapter[N, C, S]     = StationPortOutputAdapter(stationEditorController)
  lazy val inputAdapter: StationPortInputAdapter[N, C, S]       = StationPortInputAdapter(eventStream, outputAdapter)
  val stationEditorController: StationEditorController[N, C, S] = StationEditorController(inputAdapter)
  val stationEditorView: StationEditorView                      = StationEditorView(stationEditorController)

@main def stationEditor(): Unit =
  val app      = AppFrame()
  val settings = StationSettings()
  app.contents = settings.stationEditorView
  app.open()

  val initialState = AppState[N, C, S](StationMap.createCheckedStationMap())
  LazyList.continually(settings.eventStream.take()).scanLeft(initialState)((state, event) =>
    event(state)
  ).foreach((appState: AppState[N, C, S]) =>
    println(s"Stations: ${appState.stationMap.stations.length}")
  )
