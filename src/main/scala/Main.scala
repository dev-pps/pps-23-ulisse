import ulisse.applications.adapters.StationPortInputAdapter
import ulisse.applications.useCases.StationManager
import ulisse.entities.Coordinates.{Coordinate, Grid}
import ulisse.entities.station.Station
import ulisse.infrastructures.view.AppFrame
import ulisse.infrastructures.view.adapter.StationPortOutputAdapter
import ulisse.infrastructures.view.station.{StationEditorController, StationEditorView}
import ulisse.utils.Errors.BaseError

final case class Setting():
  type N = Int
  type C = Grid
  lazy val outputAdapter: StationPortOutputAdapter                = StationPortOutputAdapter(stationEditorView)
  lazy val stationManager: StationManager[N, C]                   = StationManager(outputAdapter)
  lazy val inputAdapter: StationPortInputAdapter[N, C]            = StationPortInputAdapter(stationManager)
  lazy val stationEditorController: StationEditorController[N, C] = StationEditorController(inputAdapter)
  val stationEditorView: StationEditorView                        = StationEditorView(stationEditorController)

@main def main(): Unit =
  val app      = AppFrame()
  val settings = Setting()
  app.contents = settings.stationEditorView
  app.open()
