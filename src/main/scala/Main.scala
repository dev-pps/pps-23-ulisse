import ulisse.applications.adapters.StationPortInputAdapter
import ulisse.applications.useCases.StationManager
import ulisse.entities.Coordinates.Grid
import ulisse.infrastructures.view.AppFrame
import ulisse.infrastructures.view.adapter.StationPortOutputAdapter
import ulisse.infrastructures.view.station.{StationEditorController, StationEditorView}

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
