import ulisse.applications.adapters.StationPortInputAdapter
import ulisse.applications.useCases.StationManager
import ulisse.entities.Coordinates.{Coordinate, Grid}
import ulisse.entities.station.Station
import ulisse.infrastructures.view.AppFrame
import ulisse.infrastructures.view.adapter.StationPortOutputAdapter
import ulisse.infrastructures.view.station.StationEditorController
import ulisse.utils.Errors.BaseError

//final case class Setting():
//  lazy val outputAdapter: StationPortOutputAdapter =
//    StationPortOutputAdapter(stationEditorView)
//  lazy val stationManager: StationManager[Int, Coordinate[Int]] = StationManager(outputAdapter)
//  lazy val inputAdapter: StationPortInputAdapter[Int, Coordinate[Int]] =
//    StationPortInputAdapter(stationManager)
//  lazy val stationEditorController: StationEditorController[Int, Coordinate[Int]] = StationEditorController[Int, Coordinate[Int]](inputAdapter)
//  val stationEditorView: StationEditorView                  = StationEditorView(stationEditorController)

@main def main(): Unit =
  val coord = Coordinate.createGrid(1, 1).toOption
  coord match
    case Some(c) =>
      val c1: Grid = c
      println(c)
      val station: Station[Int, Grid]                                 = Station("a", c1, 1)
      val station2: Station[Int, Coordinate[Int]]                     = station
      val cstation: Either[BaseError, Station[Int, Grid]]             = Station.createCheckedStation("a", c1, 1)
      val cstation2: Either[BaseError, Station[Int, Coordinate[Int]]] = cstation
    case None => println("Coordinate not created")
  val app = AppFrame()
//  val settings = Setting()
//  app.contents = settings.stationEditorView
  app.open()
