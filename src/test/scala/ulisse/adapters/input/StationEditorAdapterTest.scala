package ulisse.adapters.input

import cats.data.{Chain, NonEmptyChain}
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.adapters.input.StationEditorAdapter
import ulisse.applications.AppState
import ulisse.applications.managers.StationManager
import ulisse.applications.ports.StationPorts
import ulisse.applications.useCases.StationService
import ulisse.entities.Coordinate
import ulisse.entities.station.Station
import ulisse.utils.Errors.BaseError

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class StationEditorAdapterTest extends AnyWordSpec with Matchers:

  private val stationName   = "New Station"
  private val x             = 1
  private val y             = 1
  private val numberOfTrack = 1
  private val station       = Station(stationName, Coordinate(x, y), numberOfTrack)

  private val mockedPort = mock[StationPorts.Input]
  private val controller = StationEditorAdapter(mockedPort)
  private type addStationFuture  = Future[Either[NonEmptyChain[BaseError], StationPorts.Input#SM]]
  private type findStationFuture = Future[Option[Station]]
  private def addStation(): (addStationFuture, findStationFuture) =
    val addStationResult: Future[Either[NonEmptyChain[BaseError], StationPorts.Input#SM]] =
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
        when(mockedPort.addStation(station)).thenReturn(Future.successful(Right(List(station))))
        when(mockedPort.findStationAt(Coordinate(x, y))).thenReturn(Future.successful(Some(station)))
        val (addStationResult, findStationResult) = addStation()
        Await.result(addStationResult, Duration.Inf) shouldBe Right(List(station))
        Await.result(findStationResult, Duration.Inf) shouldBe Some(station)

//      "replace the station when inputs are valid and oldStation is Some(station)" in:
//        val newStation = Station(stationName, Coordinate(x + 1, y + 1), numberOfTrack + 1)
//        when(mockedPort.updateStation(station, newStation)).thenReturn(Future.successful(Right(List(newStation))))
//        val addNewStationResult =
//          controller.onOkClick(
//            stationName,
//            (x + 1).toString,
//            (y + 1).toString,
//            (numberOfTrack + 1).toString,
//            Some(station)
//          )
//
//        when(mockedPort.findStationAt(Coordinate(x + 1, y + 1))).thenReturn(Future.successful(Some(newStation)))
//        val findNewStationResult = controller.findStationAt(Coordinate(x + 1, y + 1))
//        when(mockedPort.findStationAt(Coordinate(x, y))).thenReturn(Future.successful(None))
//        val findStationAfterAddNewStationResult = controller.findStationAt(Coordinate(x, y))
//
//        Await.result(addNewStationResult, Duration.Inf) shouldBe Right(List(newStation))
//        Await.result(findNewStationResult, Duration.Inf) shouldBe Some(newStation)
//        Await.result(findStationAfterAddNewStationResult, Duration.Inf) shouldBe None

//      "chain error when inputs are valid and oldStation is not" in:
//        val oldStation = Station(stationName, Coordinate(x + 1, y + 1), numberOfTrack + 1)
//        when(mockedPort.updateStation(oldStation, station)).thenReturn(
//          Future.successful(Left(Chain(StationManager.Error.StationNotFound)))
//        )
//        val updateStationResult =
//          controller.onOkClick(
//            stationName,
//            x.toString,
//            y.toString,
//            numberOfTrack.toString,
//            Some(oldStation)
//          )
//
//        when(mockedPort.findStationAt(Coordinate(x, y))).thenReturn(Future.successful(None))
//        val findStationResult = controller.findStationAt(Coordinate(x, y))
//
//        Await.result(updateStationResult, Duration.Inf) shouldBe Left(Chain(StationManager.Error.StationNotFound))
//        Await.result(findStationResult, Duration.Inf) shouldBe None

      "chain error when same station is added" in:
        when(mockedPort.addStation(station)).thenReturn(Future.successful(Left(Chain(
          StationManager.Error.DuplicateStationName,
          StationManager.Error.DuplicateStationLocation
        ))))
        when(mockedPort.findStationAt(Coordinate(x, y))).thenReturn(Future.successful(Some(station)))
        val (addSameStationResult, findSameStationResult) = addStation()
        Await.result(addSameStationResult, Duration.Inf) shouldBe Left(Chain(
          StationManager.Error.DuplicateStationName,
          StationManager.Error.DuplicateStationLocation
        ))
        Await.result(findSameStationResult, Duration.Inf) shouldBe Some(station)

      "chain error when all inputs format are not valid" in:
        val addStationWithAllWrongInputsResult =
          controller.onOkClick(
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

      "chain error when x and y are valid and name and numberOfTracks are not" in:
        val addStationWithWrongNameAndNumberOfTrackResult =
          controller.onOkClick(
            " ",
            x.toString,
            y.toString,
            "0",
            None
          )

        Await.result(addStationWithWrongNameAndNumberOfTrackResult, Duration.Inf) shouldBe Left(
          Chain(Station.Error.InvalidName, Station.Error.InvalidNumberOfTrack)
        )
