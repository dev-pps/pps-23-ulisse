package ulisse.adapters.input

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.applications.useCases.SimulationInfoService
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.train.TrainAgent
import ulisse.entities.train.Trains.Train

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class SimulationInfoAdapterTest extends AnyWordSpec with Matchers:

  private val mockedPort         = mock[SimulationInfoPorts.Input]
  private val simulationInfoAdapter = SimulationInfoAdapter(mockedPort)

  "SimulationInfoAdapter" when:
    "query for station info" should:
      val station                   = mock[Station]
      val stationEnvironmentElement = mock[StationEnvironmentElement]
      "return the station info if present" in:
        when(mockedPort.stationInfo(station)).thenReturn(Future.successful(Some(stationEnvironmentElement)))
        Await.result(simulationInfoAdapter.stationInfo(station), Duration.Inf) shouldBe Some(stationEnvironmentElement)

      "return none if the station info is not present" in:
        when(mockedPort.stationInfo(station)).thenReturn(Future.successful(None))
        Await.result(simulationInfoAdapter.stationInfo(station), Duration.Inf) shouldBe None

    "query for route info" should:
      val route                   = mock[Route]
      val routeEnvironmentElement = mock[RouteEnvironmentElement]
      "return the route info if present" in:
        when(mockedPort.routeInfo(route)).thenReturn(Future.successful(Some(routeEnvironmentElement)))
        Await.result(simulationInfoAdapter.routeInfo(route), Duration.Inf) shouldBe Some(routeEnvironmentElement)

      "return none if the route info is not present" in:
        when(mockedPort.routeInfo(route)).thenReturn(Future.successful(None))
        Await.result(simulationInfoAdapter.routeInfo(route), Duration.Inf) shouldBe None

    "query for train info" should:
      val train      = mock[Train]
      val trainAgent = mock[TrainAgent]
      "return the train info if present" in:
        when(mockedPort.trainInfo(train)).thenReturn(Future.successful(Some(trainAgent)))
        Await.result(simulationInfoAdapter.trainInfo(train), Duration.Inf) shouldBe Some(trainAgent)

      "return none if the train info is not present" in:
        when(mockedPort.trainInfo(train)).thenReturn(Future.successful(None))
        Await.result(simulationInfoAdapter.trainInfo(train), Duration.Inf) shouldBe None
