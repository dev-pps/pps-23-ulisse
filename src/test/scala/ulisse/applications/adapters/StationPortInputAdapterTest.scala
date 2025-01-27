package ulisse.applications.adapters

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.applications.AppState
import ulisse.applications.ports.StationPorts
import ulisse.applications.station.StationMap
import ulisse.applications.station.StationMap.CheckedStationMap
import ulisse.entities.Coordinates.*
import ulisse.entities.station.Station

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Right

class StationPortInputAdapterTest extends AnyWordSpec with Matchers:

  private type N = Int
  private type C = Coordinate[N]
  private type S = Station[N, C]
  private val outputPort   = mock[StationPorts.Output]
  private val station      = Station("StationA", Coordinate(0, 0), 1)
  private val initialState = AppState[N, C, S](StationMap.createCheckedStationMap())
  private val eventStream  = LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]]()
  private val inputPort =
    StationPortInputAdapter[Int, Coordinate[Int], Station[Int, Coordinate[Int]]](eventStream, outputPort)
  private def updateState() = runAll(initialState, eventStream)

  "StationPortInputAdapter" should:
    "add a valid station to the station map" in:
      val addStationResult = inputPort.addStation(station)
      val stationMapResult = inputPort.stationMap

      updateState()
      Await.result(addStationResult, Duration.Inf) shouldBe Right(StationMap(station))
      Await.result(stationMapResult, Duration.Inf) shouldBe StationMap(station)

    "add invalid station to the station map" in:
      val addStationResult     = inputPort.addStation(station)
      val addSameStationResult = inputPort.addStation(station)
      val stationMapResult     = inputPort.stationMap

      updateState()
      Await.result(addStationResult, Duration.Inf) shouldBe Right(StationMap(station))
      Await.result(addSameStationResult, Duration.Inf) shouldBe Left(CheckedStationMap.Error.DuplicateStationName)
      Await.result(stationMapResult, Duration.Inf) shouldBe StationMap(station)

    "remove a present station from the station map" in:
      val addStationResult    = inputPort.addStation(station)
      val removeStationResult = inputPort.removeStation(station)
      val stationMapResult    = inputPort.stationMap

      updateState()
      Await.result(addStationResult, Duration.Inf) shouldBe Right(StationMap(station))
      Await.result(removeStationResult, Duration.Inf) shouldBe Right(StationMap[N, C, S]())
      Await.result(stationMapResult, Duration.Inf) shouldBe StationMap[N, C, S]()

    "not remove an absent station from the station map" in:
      val removeStationResult = inputPort.removeStation(station)
      val stationMapResult    = inputPort.stationMap
      updateState()

      Await.result(removeStationResult, Duration.Inf) shouldBe Left(CheckedStationMap.Error.StationNotFound)
      Await.result(stationMapResult, Duration.Inf) shouldBe StationMap[N, C, S]()
