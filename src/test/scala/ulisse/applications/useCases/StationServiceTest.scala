package ulisse.applications.useCases

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.applications.AppState
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.StationManager.CheckedStationManager
import ulisse.applications.ports.StationPorts
import ulisse.applications.useCases.StationService
import ulisse.entities.Coordinates.*
import ulisse.entities.station.Station

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Right

class StationServiceTest extends AnyWordSpec with Matchers:

  private type S = Station[Coordinate[Int]]
  private val station       = Station("StationA", Coordinate(0, 0), 1)
  private val initialState  = AppState[S](StationManager.createCheckedStationManager())
  private val eventStream   = LinkedBlockingQueue[AppState[S] => AppState[S]]()
  private val inputPort     = StationService[S](eventStream)
  private def updateState() = runAll(initialState, eventStream)

  "StationService" should:
    "add a valid station to the station manager" in:
      val addStationResult = inputPort.addStation(station)
      val stationMapResult = inputPort.stationMap

      updateState()
      Await.result(addStationResult, Duration.Inf) shouldBe Right(List(station))
      Await.result(stationMapResult, Duration.Inf) shouldBe List(station)

    "not add invalid station to the station manager" in:
      val addStationResult     = inputPort.addStation(station)
      val addSameStationResult = inputPort.addStation(station)
      val stationMapResult     = inputPort.stationMap

      updateState()
      Await.result(addStationResult, Duration.Inf) shouldBe Right(List(station))
      Await.result(addSameStationResult, Duration.Inf) shouldBe Left(Chain(
        CheckedStationManager.Error.DuplicateStationName,
        CheckedStationManager.Error.DuplicateStationLocation
      ))
      Await.result(stationMapResult, Duration.Inf) shouldBe List(station)

    "remove a present station from the station manager" in:
      val addStationResult    = inputPort.addStation(station)
      val removeStationResult = inputPort.removeStation(station)
      val stationMapResult    = inputPort.stationMap

      updateState()
      Await.result(addStationResult, Duration.Inf) shouldBe Right(List(station))
      Await.result(removeStationResult, Duration.Inf) shouldBe Right(List())
      Await.result(stationMapResult, Duration.Inf) shouldBe List()

    "return error when is removed an absent station from the station manager" in:
      val removeStationResult = inputPort.removeStation(station)
      val stationMapResult    = inputPort.stationMap
      updateState()

      Await.result(removeStationResult, Duration.Inf) shouldBe Left(Chain(CheckedStationManager.Error.StationNotFound))
      Await.result(stationMapResult, Duration.Inf) shouldBe List()
