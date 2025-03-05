package ulisse.adapters.input

import cats.data.{Chain, NonEmptyChain}
import org.mockito.Mockito.{spy, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.adapters.input.StationEditorAdapter
import ulisse.adapters.input.StationEditorAdapter.StationCreationInfo
import ulisse.applications.AppState
import ulisse.applications.managers.StationManager
import ulisse.applications.ports.StationPorts
import ulisse.applications.useCases.StationService
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes.Route
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

  private val station        = Station(stationName, Coordinate(x, y), numberOfTrack)
  private val updatedStation = Station(stationName, Coordinate(x + 1, y + 1), numberOfTrack + 1)

  private val mockedPort = mock[StationPorts.Input]
  private val controller = StationEditorAdapter(mockedPort)
  private type addStationFuture = Future[Either[NonEmptyChain[BaseError], StationPorts.Input#SM]]
  private def addStation(): addStationFuture =
    controller.addStation(StationCreationInfo(
      stationName,
      x.toString,
      y.toString,
      numberOfTrack.toString
    ))

  private type updateStationFuture = Future[Either[NonEmptyChain[BaseError], (StationPorts.Input#SM, List[Route])]]
  private def updateStation(): updateStationFuture =
    controller.updateStation(
      StationCreationInfo(
        stationName,
        (x + 1).toString,
        (y + 1).toString,
        (numberOfTrack + 1).toString
      ),
      station
    )

  "StationEditorController" when:
    "a station is added" should:
      "add a new station when inputs are valid" in:
        when(mockedPort.addStation(station)).thenReturn(Future.successful(Right(List(station))))
        val addStationResult = addStation()
        Await.result(addStationResult, Duration.Inf) shouldBe Right(List(station))

      "chain error when same station is added" in:
        when(mockedPort.addStation(station)).thenReturn(Future.successful(Left(Chain(
          StationManager.Error.DuplicateStationName,
          StationManager.Error.DuplicateStationLocation
        ))))
        val addStationResult = addStation()
        Await.result(addStationResult, Duration.Inf) shouldBe Left(Chain(
          StationManager.Error.DuplicateStationName,
          StationManager.Error.DuplicateStationLocation
        ))

      "chain error when all inputs format are not valid" in:
        Await.result(
          controller.addStation(StationCreationInfo(stationName, "a", "a", "a")),
          Duration.Inf
        ) shouldBe Left(
          Chain(
            StationEditorAdapter.Error.InvalidFirstCoordinateComponentFormat,
            StationEditorAdapter.Error.InvalidSecondCoordinateComponentFormat,
            StationEditorAdapter.Error.InvalidNumberOfPlatformsFormat
          )
        )

      "chain error when coordinates format are valid and name and numberOfTracks are not" in:
        Await.result(
          controller.addStation(StationCreationInfo(" ", x.toString, x.toString, "0")),
          Duration.Inf
        ) shouldBe Left(
          Chain(Station.Error.InvalidName, Station.Error.InvalidNumberOfPlatforms)
        )

    "a station is updated" should:
      "replace the station when inputs are valid" in:
        when(mockedPort.updateStation(station, updatedStation)).thenReturn(Future.successful(Right((
          List(updatedStation),
          List()
        ))))
        val updateStationResult = updateStation()
        Await.result(updateStationResult, Duration.Inf) shouldBe Right(List(updatedStation), List())

      "chain error when inputs are valid and oldStation is not" in:
        when(mockedPort.updateStation(station, updatedStation)).thenReturn(
          Future.successful(Left(Chain(StationManager.Error.StationNotFound)))
        )
        val updateStationResult = updateStation()
        Await.result(updateStationResult, Duration.Inf) shouldBe Left(Chain(StationManager.Error.StationNotFound))

    "a station is removed" should:
      "remove the station when inputs are valid" in:
        when(mockedPort.removeStation(station)).thenReturn(Future.successful(Right((List(), List()))))
        val removeStationResult = controller.removeStation(station)
        Await.result(removeStationResult, Duration.Inf) shouldBe Right((List(), List()))

      "chain error when inputs are valid and station is not present" in:
        when(mockedPort.removeStation(station)).thenReturn(
          Future.successful(Left(Chain(StationManager.Error.StationNotFound)))
        )
        val removeStationResult = controller.removeStation(station)
        Await.result(removeStationResult, Duration.Inf) shouldBe Left(Chain(StationManager.Error.StationNotFound))

    "a station is searched by location" should:
      "return the station if it exists" in:
        when(mockedPort.findStationAt(station.coordinate)).thenReturn(Future.successful(Some(station)))
        val findStationResult = controller.findStationAt(station.coordinate)
        Await.result(findStationResult, Duration.Inf) shouldBe Some(station)

      "return None if the station does not exist" in:
        when(mockedPort.findStationAt(station.coordinate)).thenReturn(Future.successful(None))
        val findStationResult = controller.findStationAt(station.coordinate)
        Await.result(findStationResult, Duration.Inf) shouldBe None
