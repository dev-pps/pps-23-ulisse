package ulisse.infrastructure.view.station

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import ulisse.applications.adapters.StationPortInputAdapter
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station
import ulisse.applications.ports.StationPorts
import ulisse.infrastructures.view.station.StationEditorController
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.applications.AppState
import ulisse.applications.station.StationMap
import ulisse.utils.Errors.BaseError

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.duration.Duration
import scala.concurrent.Await

class StationEditorControllerTest extends AnyWordSpec with Matchers:

  private type N = Int
  private type C = Coordinate[N]
  private type S = Station[N, C]

  private val outputPort    = mock[StationPorts.Output]
  private val stationName   = "New Station"
  private val x             = 1
  private val y             = 1
  private val numberOfTrack = 1
  private val station       = Station(stationName, Coordinate(x, y), numberOfTrack)
  private val initialState = AppState[N, C, S](StationMap.createCheckedStationMap())

  "StationEditorController" when:
    "onOkClick is invoked" should:
      "add a new station when inputs are valid and oldStation is None" in:
        val eventStream  = LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]]()
        val inputPort =
          StationPortInputAdapter[Int, Coordinate[Int], Station[Int, Coordinate[Int]]](eventStream, outputPort)
        val controller = StationEditorController[Int, Coordinate[Int], Station[Int, Coordinate[Int]]](inputPort)
        val station    = Station(stationName, Coordinate(x, y), numberOfTrack)

        val addStationResult =
          controller.onOkClick(
            stationName,
            x.toString,
            y.toString,
            numberOfTrack.toString,
            None
          )

        val findStationResult = controller.findStationAt(Coordinate(x, y))

        runAll(initialState, eventStream)
        Await.result(addStationResult, Duration.Inf) shouldBe Right(StationMap(station))
        Await.result(findStationResult, Duration.Inf) shouldBe Some(station)

      "replace the station when inputs are valid and oldStation is Some(station)" in:
        val eventStream  = LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]]()
        val inputPort =
          StationPortInputAdapter[Int, Coordinate[Int], Station[Int, Coordinate[Int]]](eventStream, outputPort)
        val controller = StationEditorController[Int, Coordinate[Int], Station[Int, Coordinate[Int]]](inputPort)

        val oldStation = Station(stationName, Coordinate(5, 5), numberOfTrack)
        val newStation = Station(stationName, Coordinate(x + 1, y + 1), numberOfTrack + 1)

        val addOldStationResult =
          controller.onOkClick(
            stationName,
            5.toString,
            5.toString,
            numberOfTrack.toString,
            None
          )

        val findOldStationResult = controller.findStationAt(Coordinate(5, 5))

        val addNewStationResult =
          controller.onOkClick(
            stationName,
            (x + 1).toString,
            (y + 1).toString,
            (numberOfTrack + 1).toString,
            Some(oldStation)
          )

        val findNewStationResult                   = controller.findStationAt(Coordinate(x + 1, y + 1))
        val findOldStationAfterAddNewStationResult = controller.findStationAt(Coordinate(x, y))

        runAll(initialState, eventStream)
        Await.result(addOldStationResult, Duration.Inf) shouldBe Right(StationMap(oldStation))
        Await.result(findOldStationResult, Duration.Inf) shouldBe Some(oldStation)
        Await.result(addNewStationResult, Duration.Inf) shouldBe Right(StationMap(newStation))
        Await.result(findNewStationResult, Duration.Inf) shouldBe Some(newStation)
        Await.result(findOldStationAfterAddNewStationResult, Duration.Inf) shouldBe None

      "returns error when input are not valid" in:
        val eventStream  = LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]]()
        val inputPort =
          StationPortInputAdapter[Int, Coordinate[Int], Station[Int, Coordinate[Int]]](eventStream, outputPort)
        val controller = StationEditorController[Int, Coordinate[Int], Station[Int, Coordinate[Int]]](inputPort)

        val addStationWithWrongRowResult =
          controller.onOkClick(
            stationName,
            "a",
            y.toString,
            numberOfTrack.toString,
            None
          )

        val addStationWithWrongColumnResult =
          controller.onOkClick(
            stationName,
            x.toString,
            "a",
            numberOfTrack.toString,
            None
          )

        val addStationWithWrongNumberOfTrackResult =
          controller.onOkClick(
            stationName,
            x.toString,
            y.toString,
            "a",
            None
          )

        runAll(initialState, eventStream)
        Await.result(addStationWithWrongRowResult, Duration.Inf) shouldBe Left(
          StationEditorController.Error.InvalidRowFormat
        )
        Await.result(addStationWithWrongColumnResult, Duration.Inf) shouldBe Left(
          StationEditorController.Error.InvalidColumnFormat
        )
        Await.result(addStationWithWrongNumberOfTrackResult, Duration.Inf) shouldBe Left(
          StationEditorController.Error.InvalidNumberOfTrackFormat
        )
