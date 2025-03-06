package ulisse.applications.useCases

import cats.data.Chain
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.StationManager
import ulisse.applications.{AppState, EventQueue}
import ulisse.entities.Coordinate
import ulisse.entities.Coordinate.*
import ulisse.entities.station.Station
import ulisse.entities.station.StationTest.{stationA, stationB}

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Right

class StationServiceTest extends AnyWordSpec with Matchers:

  private val mockedRouteManager = mock[RouteManager]
  when(mockedRouteManager.routes).thenReturn(List())
  private val initialState  = AppState().updateRoute(_ => mockedRouteManager)
  private val eventQueue    = EventQueue()
  private val inputPort     = StationService(eventQueue)
  private def updateState() = runAll(initialState, eventQueue.events)

  private def removeStation(): Unit =
    val addStationResult    = inputPort.addStation(stationA)
    val removeStationResult = inputPort.removeStation(stationA)
    val stationMapResult    = inputPort.stationMap
    updateState()
    Await.result(addStationResult, Duration.Inf) shouldBe Right(List(stationA))
    Await.result(removeStationResult, Duration.Inf) shouldBe Right((List(), List()))
    Await.result(stationMapResult, Duration.Inf) shouldBe List()

  private def erroneousRemoveStation(): Unit =
    val removeStationResult = inputPort.removeStation(stationA)
    val stationMapResult    = inputPort.stationMap
    updateState()
    Await.result(removeStationResult, Duration.Inf) shouldBe Left(Chain(StationManager.Error.StationNotFound))
    Await.result(stationMapResult, Duration.Inf) shouldBe List()

  "StationService" should:
    "add a valid station to the station manager" in:
      val addStationResult = inputPort.addStation(stationA)
      val stationMapResult = inputPort.stationMap

      updateState()
      Await.result(addStationResult, Duration.Inf) shouldBe Right(List(stationA))
      Await.result(stationMapResult, Duration.Inf) shouldBe List(stationA)

    "not add invalid station to the station manager" in:
      val addStationResult     = inputPort.addStation(stationA)
      val addSameStationResult = inputPort.addStation(stationA)
      val stationMapResult     = inputPort.stationMap

      updateState()
      Await.result(addStationResult, Duration.Inf) shouldBe Right(List(stationA))
      Await.result(addSameStationResult, Duration.Inf) shouldBe Left(Chain(
        StationManager.Error.DuplicateStationName,
        StationManager.Error.DuplicateStationLocation
      ))
      Await.result(stationMapResult, Duration.Inf) shouldBe List(stationA)

    "remove a present station from the station manager and the route manager" in:
      when(mockedRouteManager.deleteByStation(stationA)).thenReturn(Right(mockedRouteManager))
      removeStation()

    "remove a present station from the station manager" in:
      when(mockedRouteManager.deleteByStation(stationA)).thenReturn(Left(Chain()))
      removeStation()

    "return error when is removed an absent station from the station manager and the route manager" in:
      when(mockedRouteManager.deleteByStation(stationA)).thenReturn(Left(Chain()))
      erroneousRemoveStation()

    "return error when is removed an absent station from the station manager" in:
      when(mockedRouteManager.deleteByStation(stationA)).thenReturn(Right(mockedRouteManager))
      erroneousRemoveStation()

    "update a present station in the station manager" in:
      val addStationResult    = inputPort.addStation(stationA)
      val updateStationResult = inputPort.updateStation(stationA, stationB)
      val stationMapResult    = inputPort.stationMap
      when(mockedRouteManager.modifyAutomaticByStation(stationA, stationB)).thenReturn(mockedRouteManager)

      updateState()
      Await.result(addStationResult, Duration.Inf) shouldBe Right(List(stationA))
      Await.result(updateStationResult, Duration.Inf) shouldBe Right(List(stationB), List())
      Await.result(stationMapResult, Duration.Inf) shouldBe List(stationB)

    "return error when is updated an absent station in the station manager" in:
      val updateStationResult = inputPort.updateStation(stationA, stationB)
      val stationMapResult    = inputPort.stationMap

      updateState()
      Await.result(updateStationResult, Duration.Inf) shouldBe Left(Chain(StationManager.Error.StationNotFound))
      Await.result(stationMapResult, Duration.Inf) shouldBe List()
