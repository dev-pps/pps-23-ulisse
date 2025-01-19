import ulisse.applications.adapters.StationPortInputAdapter
import ulisse.applications.useCases.StationManager
import ulisse.entities.Coordinates.Grid
import ulisse.infrastructures.view.AppFrame
import ulisse.infrastructures.view.adapter.StationPortOutputAdapter
import ulisse.infrastructures.view.station.StationEditorView

final case class Setting():
  lazy val outputAdapter: StationPortOutputAdapter =
    StationPortOutputAdapter(stationEditor)
  lazy val stationManager: StationManager[Int, Grid] = StationManager(outputAdapter)
  lazy val inputAdapter: StationPortInputAdapter[Int, Grid] =
    StationPortInputAdapter(stationManager)
  val stationEditor: StationEditorView = StationEditorView(inputAdapter)

@main def main(): Unit =
  val app      = AppFrame()
  val settings = Setting()
  app.contents = settings.stationEditor
  app.open()
