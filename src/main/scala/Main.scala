import ulisse.applications.adapters.StationPortInputAdapter
import ulisse.applications.useCases.StationManager
import ulisse.entities.Coordinates.{Coordinate, Grid}
import ulisse.entities.station.Station
import ulisse.entities.station.Station.CheckedStation
import ulisse.infrastructures.view.AppFrame
import ulisse.infrastructures.view.adapter.StationPortOutputAdapter
import ulisse.infrastructures.view.station.{StationEditorController, StationEditorView}
import ulisse.utils.Errors.BaseError

final case class StationSettings():
  type N = Int
  type C = Grid
  type S = CheckedStation[N, C]
  val eventStream = new EventStream[BaseError]
  lazy val outputAdapter: StationPortOutputAdapter[N, C, S]     = StationPortOutputAdapter(stationEditorController)
  lazy val stationManager: StationManager[N, C, S]              = StationManager(outputAdapter)
  lazy val inputAdapter: StationPortInputAdapter[N, C, S]       = StationPortInputAdapter(stationManager)
  val stationEditorController: StationEditorController[N, C, S] = StationEditorController(inputAdapter)
  val stationEditorView: StationEditorView                      = StationEditorView(stationEditorController)

@main def stationEditor(): Unit =
  val app      = AppFrame()
  val settings = StationSettings()
  app.contents = settings.stationEditorView
  app.open()
