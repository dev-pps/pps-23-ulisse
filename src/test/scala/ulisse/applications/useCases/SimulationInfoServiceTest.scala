package ulisse.applications.useCases

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.applications.AppState
import ulisse.entities.route.{RouteEnvironmentElement, Track}
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.Environments.RailwayEnvironment
import ulisse.entities.station.{Platform, Station, StationEnvironmentElement}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.Train

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SimulationInfoServiceTest extends AnyWordSpec with Matchers:
  private val mockedStationName = "mockedStation"
  private val mockedStation     = mock[Station]
  when(mockedStation.name).thenReturn(mockedStationName)
  private val mockedStationEE = mock[StationEnvironmentElement]
  when(mockedStationEE.name).thenReturn(mockedStationName)
  private val otherStationName   = "otherStation"
  private val otherMockedStation = mock[Station]
  when(otherMockedStation.name).thenReturn(otherStationName)

  private val mockedRouteId = 1000
  private val mockedRoute   = mock[Route]
  when(mockedRoute.id).thenReturn(mockedRouteId)
  private val mockedRouteEE = mock[RouteEnvironmentElement]
  when(mockedRouteEE.id).thenReturn(mockedRouteId)
  private val otherRouteId     = 2000
  private val otherMockedRoute = mock[Route]
  when(otherMockedRoute.id).thenReturn(otherRouteId)

  private val mockedTrainName = "mockedTrain"
  private val mockedTrain     = mock[Train]
  when(mockedTrain.name).thenReturn(mockedTrainName)
  private val mockedTrainAgent = mock[TrainAgent]
  when(mockedTrainAgent.name).thenReturn(mockedTrainName)
  // needed because train agents are retrieved from the environment elements
  when(mockedStationEE.containers).thenReturn(Seq(Platform(1)))
  when(mockedRouteEE.containers).thenReturn(Seq(Track(1, mockedTrainAgent)(using 100)))
  private val otherTrainName   = "otherTrain"
  private val otherMockedTrain = mock[Train]
  when(otherMockedTrain.name).thenReturn(otherTrainName)

  private val initialState = AppState.default().swap(_.simulationManager.setupEnvironment(
    RailwayEnvironment(
      Seq(mockedStationEE),
      Seq(mockedRouteEE),
      Seq(mockedTrainAgent)
    )
  ))

  private val eventQueue            = LinkedBlockingQueue[AppState => AppState]()
  private val simulationInfoService = SimulationInfoService(eventQueue)

  private def updateState() = runAll(initialState, eventQueue)
  "SimulationInfoService" should:
    "return station info if station is in the manager" in:
      val stationInfoResult = simulationInfoService.stationInfo(mockedStation)
      updateState()
      Await.result(stationInfoResult, Duration.Inf) shouldBe Some(mockedStationEE)

    "return None if station is not in the manager" in:
      val stationInfoResult = simulationInfoService.stationInfo(otherMockedStation)
      updateState()
      Await.result(stationInfoResult, Duration.Inf) shouldBe None

    "return route info" in:
      val routeInfoResult = simulationInfoService.routeInfo(mockedRoute)
      updateState()
      Await.result(routeInfoResult, Duration.Inf) shouldBe Some(mockedRouteEE)

    "return None if route is not in the manager" in:
      val routeInfoResult = simulationInfoService.routeInfo(otherMockedRoute)
      updateState()
      Await.result(routeInfoResult, Duration.Inf) shouldBe None

    "return train info" in:
      val trainInfoResult = simulationInfoService.trainInfo(mockedTrain)
      updateState()
      Await.result(trainInfoResult, Duration.Inf) shouldBe Some(mockedTrainAgent)

    "return None if train is not in the manager" in:
      val trainInfoResult = simulationInfoService.trainInfo(otherMockedTrain)
      updateState()
      Await.result(trainInfoResult, Duration.Inf) shouldBe None
