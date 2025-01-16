import applications.adapters.StationPortInputAdapter
import applications.usecase.StationManager
import entities.Location.Grid
import infrastructures.ui.AppFrame
import infrastructures.ui.adapter.StationPortOutputAdapter
import infrastructures.ui.station.StationEditorView

final case class Setting():
  lazy val outputAdapter: StationPortOutputAdapter =
    StationPortOutputAdapter(stationEditor)
  lazy val stationManager: StationManager[Grid] = StationManager(outputAdapter)
  lazy val inputAdapter: StationPortInputAdapter[Grid] =
    StationPortInputAdapter(stationManager)
  val stationEditor: StationEditorView = StationEditorView(inputAdapter)

@main def main(): Unit =
  val app      = AppFrame()
  val settings = Setting()
  app.contents = settings.stationEditor
  app.open()
