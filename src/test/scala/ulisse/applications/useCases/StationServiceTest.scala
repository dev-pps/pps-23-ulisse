package ulisse.applications.useCases

import cats.data.Chain
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.{AppState, EventQueue}
import ulisse.entities.Coordinate
import ulisse.entities.Coordinate.*
import ulisse.entities.route.RouteEnvironmentElementTest.{routeAB, routeBC, routeCD, routeDE}
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.entities.station.StationTest.{stationA, stationB}

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Right

class StationServiceTest extends AnyWordSpec with Matchers with BeforeAndAfterEach:

  private val mockedTimetableManager = mock[TimetableManager]
  private val mockedRouteManager     = mock[RouteManager]
  private val newMockedRouteManager  = mock[RouteManager]
  private val routeList              = List(routeAB, routeBC)
  private val updatedRouteList       = List(routeCD, routeDE)
  private val initialState =
    AppState().updateRoute(_ => mockedRouteManager).updateTimetable(_ => mockedTimetableManager)
  private val eventQueue    = EventQueue()
  private val inputPort     = StationService(eventQueue)
  private def updateState() = runAll(initialState, eventQueue.events)

  private def removeStation(expectedRoutes: List[Route]): Unit =
    val addStationResult    = inputPort.addStation(stationA)
    val removeStationResult = inputPort.removeStation(stationA)
    val stationMapResult    = inputPort.stationMap
    updateState()
    Await.result(addStationResult, Duration.Inf) shouldBe Right(List(stationA))
    Await.result(removeStationResult, Duration.Inf) shouldBe Right((List(), expectedRoutes))
    Await.result(stationMapResult, Duration.Inf) shouldBe List()

  private def erroneousRemoveStation(): Unit =
    val removeStationResult = inputPort.removeStation(stationA)
    val stationMapResult    = inputPort.stationMap
    updateState()
    Await.result(removeStationResult, Duration.Inf) shouldBe Left(Chain(StationManager.Error.StationNotFound))
    Await.result(stationMapResult, Duration.Inf) shouldBe List()

  override def beforeEach(): Unit =
    reset(mockedRouteManager, mockedTimetableManager)
    when(mockedRouteManager.routes).thenReturn(routeList)
    when(newMockedRouteManager.routes).thenReturn(updatedRouteList)
    when(mockedTimetableManager.routeDeleted(routeAB)).thenReturn(Right(mockedTimetableManager))
    when(mockedTimetableManager.routeDeleted(routeBC)).thenReturn(Right(mockedTimetableManager))
    when(mockedTimetableManager.routeUpdated(routeAB, routeCD)).thenReturn(Right(mockedTimetableManager))
    when(mockedTimetableManager.routeUpdated(routeBC, routeDE)).thenReturn(Right(mockedTimetableManager))

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
      when(mockedRouteManager.deleteByStation(stationA)).thenReturn(Right(newMockedRouteManager))

      removeStation(updatedRouteList)
      verify(mockedTimetableManager).routeDeleted(routeAB)
      verify(mockedTimetableManager).routeDeleted(routeBC)

    "remove a present station from the station manager" in:
      when(mockedRouteManager.deleteByStation(stationA)).thenReturn(Left(Chain()))
      removeStation(routeList)
      verifyNoInteractions(mockedTimetableManager)

    "return error when is removed an absent station from the station manager and the route manager" in:
      when(mockedRouteManager.deleteByStation(stationA)).thenReturn(Left(Chain()))
      erroneousRemoveStation()
      verifyNoInteractions(mockedTimetableManager)

    "return error when is removed an absent station from the station manager" in:
      when(mockedRouteManager.deleteByStation(stationA)).thenReturn(Right(newMockedRouteManager))
      erroneousRemoveStation()
      verifyNoInteractions(mockedRouteManager)
      verifyNoInteractions(mockedTimetableManager)

    "update a present station in the station manager" in:
      val addStationResult    = inputPort.addStation(stationA)
      val updateStationResult = inputPort.updateStation(stationA, stationB)
      val stationMapResult    = inputPort.stationMap
      when(mockedRouteManager.modifyAutomaticByStation(stationA, stationB)).thenReturn(newMockedRouteManager)

      updateState()
      Await.result(addStationResult, Duration.Inf) shouldBe Right(List(stationA))
      Await.result(updateStationResult, Duration.Inf) shouldBe Right(List(stationB), updatedRouteList)
      Await.result(stationMapResult, Duration.Inf) shouldBe List(stationB)
      verify(mockedTimetableManager).routeUpdated(routeAB, routeCD)
      verify(mockedTimetableManager).routeUpdated(routeBC, routeDE)

    "return error when is updated an absent station in the station manager" in:
      val updateStationResult = inputPort.updateStation(stationA, stationB)
      val stationMapResult    = inputPort.stationMap

      updateState()
      Await.result(updateStationResult, Duration.Inf) shouldBe Left(Chain(StationManager.Error.StationNotFound))
      Await.result(stationMapResult, Duration.Inf) shouldBe List()
      verifyNoInteractions(mockedRouteManager)
      verifyNoInteractions(mockedTimetableManager)
