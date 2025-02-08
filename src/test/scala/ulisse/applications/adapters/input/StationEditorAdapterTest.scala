package ulisse.applications.adapters.input

import cats.data.{Chain, NonEmptyChain}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.adapters.input.StationEditorAdapter
import ulisse.applications.AppState
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.StationManager.CheckedStationManager
import ulisse.applications.ports.StationPorts
import ulisse.applications.useCases.StationService
import ulisse.entities.Coordinates.{Coordinate, Grid}
import ulisse.entities.station.Station
import ulisse.entities.station.Station.CheckedStation
import ulisse.utils.Errors.BaseError

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class StationEditorAdapterTest extends AnyWordSpec with Matchers:

  private val stationName   = "New Station"
  private val x             = 1
  private val y             = 1
  private val numberOfTrack = 1

  private def configureTest[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](
      using
      coordinateGenerator: (N, N) => Either[NonEmptyChain[BaseError], C],
      stationGenerator: (String, C, Int) => Either[NonEmptyChain[BaseError], S]
  ): (StationEditorAdapter[N, C, S], () => List[AppState[N, C, S]]) =
    val station      = Station(stationName, Coordinate(x, y), numberOfTrack)
    val initialState = AppState[N, C, S](StationManager.createCheckedStationManager())
    val eventStream  = LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]]()
    val inputPort =
      StationService[N, C, S](eventStream)
    val controller = StationEditorAdapter[N, C, S](inputPort)
    (controller, () => runAll(initialState, eventStream))

  private val station                   = Station(stationName, Coordinate(x, y), numberOfTrack)
  private val (controller, updateState) = configureTest[Int, Coordinate[Int], Station[Int, Coordinate[Int]]]()
  private val (checkedController, _)    = configureTest[Int, Grid, CheckedStation[Int, Grid]]()

  private type addStationFuture =
    Future[Either[
      NonEmptyChain[BaseError],
      StationManager[Int, Coordinate[Int], Station[Int, Coordinate[Int]]]#StationMapType
    ]]
  private type findStationFuture = Future[Option[Station[Int, Coordinate[Int]]]]
  private def addStation(): (addStationFuture, findStationFuture) =
    val addStationResult =
      controller.onOkClick(
        stationName,
        x.toString,
        y.toString,
        numberOfTrack.toString,
        None
      )
    val findStationResult = controller.findStationAt(Coordinate(x, y))
    (addStationResult, findStationResult)

  "StationEditorController" when:
    "onOkClick is invoked" should:
      "add a new station when inputs are valid and oldStation is None" in:
        val (addStationResult, findStationResult) = addStation()
        updateState()
        Await.result(addStationResult, Duration.Inf) shouldBe Right(List(station))
        Await.result(findStationResult, Duration.Inf) shouldBe Some(station)

      "replace the station when inputs are valid and oldStation is Some(station)" in:
        val (addStationResult, findStationResult) = addStation()
        val newStation                            = Station(stationName, Coordinate(x + 1, y + 1), numberOfTrack + 1)

        val addNewStationResult =
          controller.onOkClick(
            stationName,
            (x + 1).toString,
            (y + 1).toString,
            (numberOfTrack + 1).toString,
            Some(station)
          )

        val findNewStationResult                = controller.findStationAt(Coordinate(x + 1, y + 1))
        val findStationAfterAddNewStationResult = controller.findStationAt(Coordinate(x, y))

        updateState()
        Await.result(addStationResult, Duration.Inf) shouldBe Right(List(station))
        Await.result(findStationResult, Duration.Inf) shouldBe Some(station)
        Await.result(addNewStationResult, Duration.Inf) shouldBe Right(List(newStation))
        Await.result(findNewStationResult, Duration.Inf) shouldBe Some(newStation)
        Await.result(findStationAfterAddNewStationResult, Duration.Inf) shouldBe None

      "chain error when same station is added" in:
        val (addStationResult, findStationResult)         = addStation()
        val (addSameStationResult, findSameStationResult) = addStation()

        updateState()
        Await.result(addStationResult, Duration.Inf) shouldBe Right(List(station))
        Await.result(findStationResult, Duration.Inf) shouldBe Some(station)
        Await.result(addSameStationResult, Duration.Inf) shouldBe Left(Chain(
          CheckedStationManager.Error.DuplicateStationName,
          CheckedStationManager.Error.DuplicateStationLocation
        ))
        Await.result(findSameStationResult, Duration.Inf) shouldBe Some(station)

      "chain error when all inputs format are not valid" in:
        val addStationWithAllWrongInputsResult =
          checkedController.onOkClick(
            stationName,
            "a",
            "a",
            "a",
            None
          )

        Await.result(addStationWithAllWrongInputsResult, Duration.Inf) shouldBe Left(
          Chain(
            StationEditorAdapter.Error.InvalidFirstCoordinateComponentFormat,
            StationEditorAdapter.Error.InvalidSecondCoordinateComponentFormat,
            StationEditorAdapter.Error.InvalidNumberOfTrackFormat
          )
        )

      "chain error when all inputs are not valid" in:
        val addStationWithAllWrongInputsResult =
          checkedController.onOkClick(
            " ",
            "-1",
            "-1",
            "0",
            None
          )

        Await.result(addStationWithAllWrongInputsResult, Duration.Inf) shouldBe Left(
          Chain(
            Grid.Error.InvalidRow,
            Grid.Error.InvalidColumn
          )
        )

      "chain error when x and y are valid and name and numberOfTracks are not" in:
        val addStationWithWrongNameAndNumberOfTrackResult =
          checkedController.onOkClick(
            " ",
            x.toString,
            y.toString,
            "0",
            None
          )

        Await.result(addStationWithWrongNameAndNumberOfTrackResult, Duration.Inf) shouldBe Left(
          Chain(CheckedStation.Error.InvalidName, CheckedStation.Error.InvalidNumberOfTrack)
        )
