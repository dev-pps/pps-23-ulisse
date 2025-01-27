import StationTypes.*
import ulisse.applications.AppState
import ulisse.applications.adapters.StationPortInputAdapter
import ulisse.applications.station.StationMap
import ulisse.entities.Coordinates.Grid
import ulisse.entities.station.Station
import ulisse.entities.station.Station.CheckedStation
import ulisse.infrastructures.view.AppFrame
import ulisse.infrastructures.view.adapter.StationPortOutputAdapter
import ulisse.infrastructures.view.station.{StationEditorController, StationEditorView}

import java.util.concurrent.LinkedBlockingQueue

@main def stationEditor(): Unit =
  val app      = AppFrame()
  val settings = StationSettings()
  app.contents = settings.stationEditorView
  app.open()

  val initialState = AppState[N, C, S](StationMap.createCheckedStationMap())
  LazyList.continually(settings.eventStream.take()).foldLeft(initialState)((state, event) =>
    event(state)
  )

final case class StationSettings():
  lazy val outputAdapter: StationPortOutputAdapter[N, C, S] = StationPortOutputAdapter(stationEditorController)
  lazy val inputAdapter: StationPortInputAdapter[N, C, S]   = StationPortInputAdapter(eventStream, outputAdapter)
  val eventStream = LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]]()
  val stationEditorController: StationEditorController[N, C, S] = StationEditorController(inputAdapter)
  val stationEditorView: StationEditorView                      = StationEditorView(stationEditorController)

object StationTypes:
  type N = Int
  type C = Grid
  type S = CheckedStation[N, C]
