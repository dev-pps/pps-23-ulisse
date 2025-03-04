package ulisse.entities.simulation.environments.railwayEnvironment

import org.mockito.Mockito.{spy, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Tracks.TrackDirection
import ulisse.entities.train.TrainAgents.{TrainAgent, TrainAgentPerception, TrainPerceptionInStation, TrainRouteInfo, TrainStationInfo}
import ulisse.entities.simulation.environments.EnvironmentElements.TrainAgentEEWrapper.findIn
import ulisse.entities.simulation.agents.Perceptions.PerceptionProvider
import ulisse.entities.simulation.environments.railwayEnvironment.PerceptionProvider.given
import ulisse.entities.station.StationEnvironments.StationEnvironmentElement
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.utils.Times.{ClockTime, Time}
class PerceptionProviderTest extends AnyWordSpec with Matchers:

  private val mockStationA = mock[StationEnvironmentElement]
  private val mockStationB = mock[StationEnvironmentElement]
  private val trainAgent = mock[TrainAgent]
  private val see = mock[StationEnvironmentElement]
  private val ree = mock[RouteEnvironmentElement]
  private val railwayEnvironment = mock[RailwayEnvironment]
  private val dtt = mock[DynamicTimetable]
  private val baseClock = ClockTime(0, 0)
  private val defaultClockTime = baseClock.toOption
  when(railwayEnvironment.stations).thenReturn(Seq(see))
  when(railwayEnvironment.routes).thenReturn(Seq(ree))
  when(railwayEnvironment.time).thenReturn(baseClock.getOrDefault.asTime)

  private val perceptionProvider = summon[PerceptionProvider[RailwayEnvironment, TrainAgent]]

  private def trainInStation(): Unit =
    when(see.contains(trainAgent)).thenReturn(true)
    when(ree.contains(trainAgent)).thenReturn(false)

  private def trainInStationWithTimeTable(timetable: Option[DynamicTimetable]): Unit =
    trainInStation()
    when(railwayEnvironment.findCurrentTimeTableFor(trainAgent)).thenReturn(timetable)

  private def trainInStationWithNextDepartureTime(nextDepartureTime: Option[ClockTime]): Unit =
    trainInStationWithTimeTable(Some(dtt))
    when(dtt.nextDepartureTime).thenReturn(nextDepartureTime)

  private def trainInStationWithNextDepartureTimeAndRouteInfo(
                                           nextDepartureTime: Option[ClockTime],
                                           routeInfo: Option[(RouteEnvironmentElement, TrackDirection)]): Unit =
    trainInStationWithNextDepartureTime(nextDepartureTime)
    when(dtt.nextRoute).thenReturn(Some((mockStationA, mockStationB)))
    when(railwayEnvironment.findRouteWithTravelDirection((mockStationA, mockStationB))).thenReturn(routeInfo)

  private def trainInStationWithNextDepartureTimeAndRoute(
                                             nextDepartureTime: Option[ClockTime],
                                             available: Boolean): Unit =
    trainInStationWithNextDepartureTimeAndRouteInfo(nextDepartureTime, Some((ree, TrackDirection.Forward)))
    when(ree.isAvailable(TrackDirection.Forward)).thenReturn(available)

  private def trainInRoute(): Unit =
    when(see.contains(trainAgent)).thenReturn(false)
    when(ree.contains(trainAgent)).thenReturn(true)

  private def trainInRouteWithContainers(containers: Seq[RouteEnvironmentElement]): Unit =
    trainInRoute()
    when(ree.containers).thenReturn(containers)

  "PerceptionProvider" when:
    "queried for a perception" should:
      "provide a perception for a train in a station" in:
        trainInStationWithTimeTable(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe a[Some[TrainAgentPerception[TrainStationInfo]]]

      "provide a perception for a train in a route" in:
        trainInRouteWithContainers(Seq())
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe a[Some[TrainAgentPerception[TrainRouteInfo]]]

      "prioritize station perception over route perception" in:
        when(see.contains(trainAgent)).thenReturn(true)
        when(ree.contains(trainAgent)).thenReturn(true)
        when(railwayEnvironment.findCurrentTimeTableFor(trainAgent)).thenReturn(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe a[Some[TrainAgentPerception[TrainStationInfo]]]

      "return None if the train is not in a station or route" in:
        when(see.contains(trainAgent)).thenReturn(false)
        when(ree.contains(trainAgent)).thenReturn(false)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe None

    "return a perception for a train in a station" should:
      "provide a default perception if train hasn't a current timetable" in:
        trainInStationWithTimeTable(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(false, false)))

      "provide a default perception if train hasn't a next departure time" in:
        trainInStationWithNextDepartureTime(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(false, false)))

      "provide a default perception if train hasn't a next route" in:
        trainInStationWithNextDepartureTime(defaultClockTime)
        when(dtt.nextRoute).thenReturn(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(false, false)))

      "provide a default perception if next route is not found in the env" in:
        trainInStationWithNextDepartureTimeAndRouteInfo(defaultClockTime, None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(false, false)))

      "provide a perception when departure time is greater than current time and the direction is unavailable" in:
        trainInStationWithNextDepartureTimeAndRoute(defaultClockTime + ClockTime(0,1).toOption, false)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(false, false)))

      "provide a perception when departure time is greater than current time and the direction is available" in :
        trainInStationWithNextDepartureTimeAndRoute(defaultClockTime + ClockTime(0, 1).toOption, true)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(false, true)))

      "provide a perception when departure time is lesser or equal than current time and the direction is unavailable" in :
        trainInStationWithNextDepartureTimeAndRoute(defaultClockTime, false)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(true, false)))

      "provide a perception when departure time is lesser or equal than current time and the direction is available" in :
        trainInStationWithNextDepartureTimeAndRoute(defaultClockTime, true)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(true, true)))