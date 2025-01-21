import ulisse.applications.adapters.StationPortInputAdapter
import ulisse.applications.useCases.StationManager
import ulisse.entities.Coordinates.{Coordinate, Grid}
import ulisse.entities.station.Station
import ulisse.entities.station.Station.CheckedStation
import ulisse.infrastructures.view.AppFrame
import ulisse.infrastructures.view.adapter.StationPortOutputAdapter
import ulisse.infrastructures.view.station.{StationEditorController, StationEditorView}
import ulisse.utils.Errors.BaseError

final case class Setting():
  type N = Int
  type C = Grid
  type S = CheckedStation[N, C]
  lazy val outputAdapter: StationPortOutputAdapter[N, C, S]          = StationPortOutputAdapter(stationEditorController)
  lazy val stationManager: StationManager[N, C, S]                   = StationManager(outputAdapter)
  lazy val inputAdapter: StationPortInputAdapter[N, C, S]            = StationPortInputAdapter(stationManager)
  lazy val stationEditorController: StationEditorController[N, C, S] = StationEditorController(inputAdapter)
  val stationEditorView: StationEditorView                           = StationEditorView(stationEditorController)

@main def main(): Unit =
  val app      = AppFrame()
  val settings = Setting()
  app.contents = settings.stationEditorView
  app.open()

trait Map {
  def findElement(key: String): Int
}

// Trait for CheckedMap (returns Either[String, Int])
trait CheckedMap {
  def findElement(key: String): Either[String, Int]
}
