package ulisse.applications.useCases

import cats.Id
import org.mockito.Mockito.{spy, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.applications.{AppState, EventQueue}
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.RouteEnvironmentElementTest.{routeAB, routeAB_EE, routeBC}
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.environments.railwayEnvironment.{ConfigurationData, RailwayEnvironment}
import ulisse.entities.station.Station
import ulisse.entities.station.StationEnvironmentElementTest.stationA_EE
import ulisse.entities.station.StationEnvironments.{StationEnvironmentElement, StationEnvironmentInfo}
import ulisse.entities.station.StationTest.{stationA, stationB}
import ulisse.entities.timetable.DynamicTimetableTest.{dynamicTimetable1, dynamicTimetable2}
import ulisse.entities.train.TrainAgentTest.{train3905, train3906, train3907, trainAgent3905, trainAgent3906}
import ulisse.entities.train.TrainAgents.{TrainAgent, TrainAgentInfo}
import ulisse.utils.Times.Time

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SimulationInfoServiceTest extends AnyWordSpec with Matchers:
  private val dt1 = spy(dynamicTimetable1)
  private val dt2 = spy(dynamicTimetable2)
  private val initialState = AppState().setupSimulation:
    _.simulationManager.setupEnvironment:
      RailwayEnvironment.auto:
        ConfigurationData(
          Seq(stationA_EE),
          Seq(routeAB_EE),
          Seq(trainAgent3905, trainAgent3906),
          Seq(dt1, dt2)
        )

  private val eventQueue            = EventQueue()
  private val simulationInfoService = SimulationInfoService(eventQueue)
  private def updateState()         = runAll(initialState, eventQueue.events)

  "SimulationInfoService" when:
    "queried for station info" should:
      def checkStationInfoWithDelay(delay: Time): Unit =
        when(dt1.delayIn(stationA)).thenReturn(Some(delay))
        when(dt2.delayIn(stationA)).thenReturn(Some(delay))
        val stationInfoResult = simulationInfoService.stationInfo(stationA)
        updateState()
        Await.result(stationInfoResult, Duration.Inf) match
          case Some(StationEnvironmentInfo(s, ct, at)) => (s, ct, at) shouldBe (stationA, Id(delay) + Id(delay), delay)
          case None                               => fail()

      "return station info" in:
        checkStationInfoWithDelay(Time(0, 0, 0))
        checkStationInfoWithDelay(Time(0, 10, 5))

    "return None if station is not in the manager" in:
      val stationInfoResult = simulationInfoService.stationInfo(stationB)
      updateState()
      Await.result(stationInfoResult, Duration.Inf) shouldBe None

    "return route info" in:
      val routeInfoResult = simulationInfoService.routeInfo(routeAB)
      updateState()
      Await.result(routeInfoResult, Duration.Inf) shouldBe Some(routeAB_EE)

    "return None if route is not in the manager" in:
      val routeInfoResult = simulationInfoService.routeInfo(routeBC)
      updateState()
      Await.result(routeInfoResult, Duration.Inf) shouldBe None

    "return train info" in:
      val trainInfoResult = simulationInfoService.trainInfo(train3905)
      updateState()
      Await.result(trainInfoResult, Duration.Inf) shouldBe Some(TrainAgentInfo(trainAgent3905, List(dt1, dt2)))

    "return None if train hasn't a schedule" in:
      val trainInfoResult = simulationInfoService.trainInfo(train3906)
      updateState()
      Await.result(trainInfoResult, Duration.Inf) shouldBe None

    "return None if train is not in the manager" in:
      val trainInfoResult = simulationInfoService.trainInfo(train3907)
      updateState()
      Await.result(trainInfoResult, Duration.Inf) shouldBe None
