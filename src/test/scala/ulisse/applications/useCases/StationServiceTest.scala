package ulisse.applications.useCases

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.Runner.runAll
import ulisse.applications.AppState
import ulisse.applications.managers.StationManager
import ulisse.entities.Coordinate
import ulisse.entities.Coordinate.*
import ulisse.entities.station.Station

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Right

class StationServiceTest extends AnyWordSpec with Matchers:

  private val station       = Station("StationA", Coordinate(0, 0), 1)
  private val initialState  = AppState(StationManager())
  private val eventStream   = LinkedBlockingQueue[AppState => AppState]()
  private val inputPort     = StationService(eventStream)
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
        StationManager.Error.DuplicateStationName,
        StationManager.Error.DuplicateStationLocation
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

      Await.result(removeStationResult, Duration.Inf) shouldBe Left(Chain(StationManager.Error.StationNotFound))
      Await.result(stationMapResult, Duration.Inf) shouldBe List()

    "update a present station in the station manager" in:
      val addStationResult    = inputPort.addStation(station)
      val newStation          = Station("StationB", Coordinate(1, 1), 1)
      val updateStationResult = inputPort.updateStation(station, newStation)
      val stationMapResult    = inputPort.stationMap

      updateState()
      Await.result(addStationResult, Duration.Inf) shouldBe Right(List(station))
      Await.result(updateStationResult, Duration.Inf) shouldBe Right(List(newStation))
      Await.result(stationMapResult, Duration.Inf) shouldBe List(newStation)

    "return error when is updated an absent station in the station manager" in:
      val newStation          = Station("StationB", Coordinate(1, 1), 1)
      val updateStationResult = inputPort.updateStation(station, newStation)
      val stationMapResult    = inputPort.stationMap

      updateState()
      Await.result(updateStationResult, Duration.Inf) shouldBe Left(Chain(StationManager.Error.StationNotFound))
      Await.result(stationMapResult, Duration.Inf) shouldBe List()
