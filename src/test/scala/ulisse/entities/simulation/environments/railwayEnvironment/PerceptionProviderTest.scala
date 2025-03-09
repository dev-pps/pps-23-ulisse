package ulisse.entities.simulation.environments.railwayEnvironment

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.route.Routes.RouteType.AV
import ulisse.entities.route.Tracks.{Track, TrackDirection}
import ulisse.entities.route.{RouteEnvironment, RouteEnvironmentElement}
import ulisse.entities.simulation.agents.Perceptions.PerceptionProvider
import ulisse.entities.simulation.environments.railwayEnvironment.PerceptionProviders.given
import ulisse.entities.station.{StationEnvironment, StationEnvironmentElement}
import ulisse.entities.timetable.DynamicTimetableEnvironment
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.TrainAgentPerceptions.{
  TrainAgentPerception,
  TrainPerceptionInRoute,
  TrainPerceptionInStation,
  TrainRouteInfo,
  TrainStationInfo
}
import ulisse.entities.train.TrainAgentTest.normalTrainAgent
import ulisse.entities.train.TrainAgents.*
import ulisse.utils.Times.ClockTime
class PerceptionProviderTest extends AnyWordSpec with Matchers:
  private val mockStationA = mock[StationEnvironmentElement]
  private val mockStationB = mock[StationEnvironmentElement]
  private val see          = mock[StationEnvironmentElement]
  private val se           = mock[StationEnvironment]

  private val mockedTrack = mock[Track]
  private val trainAgent  = mock[TrainAgent]
  when(trainAgent.distanceTravelled).thenReturn(0.0)
  when(mockedTrack.contains(trainAgent)).thenReturn(true)
  private val otherTrainAgent = mock[TrainAgent]

  private val ree           = mock[RouteEnvironmentElement]
  private val re            = mock[RouteEnvironment]
  private val routeTypology = AV
  private val routeLength   = 100.0
  when(ree.typology).thenReturn(routeTypology)
  when(ree.length).thenReturn(routeLength)

  private val dynamicTimetableEnvironment = mock[DynamicTimetableEnvironment]
  private val railwayEnvironment          = mock[RailwayEnvironment]
  private val dtt                         = mock[DynamicTimetable]
  private val baseClock                   = ClockTime(0, 0)
  private val defaultClockTime            = baseClock.toOption
  when(railwayEnvironment.stations).thenReturn(Seq(see))
  when(railwayEnvironment.routes).thenReturn(Seq(ree))
  when(railwayEnvironment.routeEnvironment).thenReturn(re)
  when(railwayEnvironment.stationEnvironment).thenReturn(se)
  when(railwayEnvironment.time).thenReturn(baseClock.getOrDefault.asTime)
  when(railwayEnvironment.dynamicTimetableEnvironment).thenReturn(dynamicTimetableEnvironment)
  private val perceptionProvider = summon[PerceptionProvider[RailwayEnvironment, TrainAgent]]

  private def trainInStation(): Unit =
    when(see.contains(trainAgent)).thenReturn(true)
    when(ree.contains(trainAgent)).thenReturn(false)

  private def trainInStationWithTimeTable(timetable: Option[DynamicTimetable]): Unit =
    trainInStation()
    when(railwayEnvironment.dynamicTimetableEnvironment.findCurrentTimetableFor(trainAgent)).thenReturn(timetable)

  private def trainInStationWithNextDepartureTime(nextDepartureTime: Option[ClockTime]): Unit =
    trainInStationWithTimeTable(Some(dtt))
    when(dtt.nextDepartureTime).thenReturn(nextDepartureTime)

  private def trainInStationWithNextDepartureTimeAndRouteInfo(
      nextDepartureTime: Option[ClockTime],
      routeInfo: Seq[(RouteEnvironmentElement, TrackDirection)]
  ): Unit =
    trainInStationWithNextDepartureTime(nextDepartureTime)
    when(dtt.nextRoute).thenReturn(Some((mockStationA, mockStationB)))
    when(re.findRoutesWithTravelDirection((mockStationA, mockStationB))).thenReturn(routeInfo)

  private def trainInStationWithNextDepartureTimeAndRoute(
      nextDepartureTime: Option[ClockTime],
      available: Boolean
  ): Unit =
    trainInStationWithNextDepartureTimeAndRouteInfo(nextDepartureTime, Seq((ree, TrackDirection.Forward)))
    when(ree.isAvailableFor(trainAgent, TrackDirection.Forward)).thenReturn(available)

  private def trainInRoute(): Unit =
    when(see.contains(trainAgent)).thenReturn(false)
    when(ree.contains(trainAgent)).thenReturn(true)

  private def trainInRouteWithContainers(containers: Seq[Track]): Unit =
    trainInRoute()
    when(ree.containers).thenReturn(containers)

  private def trainInRouteWithTimeTable(timetable: Option[DynamicTimetable]): Unit =
    trainInRouteWithContainers(Seq())
    when(railwayEnvironment.dynamicTimetableEnvironment.findCurrentTimetableFor(trainAgent)).thenReturn(timetable)

  private def trainInRouteWithStationInfo(available: Boolean): Unit =
    when(mockStationB.id).thenReturn(1)
    when(see.id).thenReturn(1)
    trainInRouteWithTimeTable(Some(dtt))
    when(dtt.currentRoute).thenReturn(Some((mockStationA, mockStationB)))
    when(see.isAvailable).thenReturn(available)

  def trainInRouteWithStationInfoAndTrainAhead(available: Boolean, distance: Double): Unit =
    trainInRouteWithStationInfo(available)
    trainInRouteWithContainers(Seq(mockedTrack))
    when(mockedTrack.trains).thenReturn(Seq(otherTrainAgent))
    when(otherTrainAgent.distanceTravelled).thenReturn(distance)

  "PerceptionProvider" when:
    "queried for a perception" should:
      "provide a perception for a train in a station" in:
        trainInStationWithTimeTable(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe a[
          Some[TrainAgentPerception[TrainStationInfo]]
        ]

      "provide a perception for a train in a route" in:
        trainInRouteWithTimeTable(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe a[
          Some[TrainAgentPerception[TrainRouteInfo]]
        ]

      "prioritize station perception over route perception" in:
        when(see.contains(trainAgent)).thenReturn(true)
        when(ree.contains(trainAgent)).thenReturn(true)
        when(railwayEnvironment.dynamicTimetableEnvironment.findCurrentTimetableFor(trainAgent)).thenReturn(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe a[
          Some[TrainAgentPerception[TrainStationInfo]]
        ]

      "return None if the train is not in a station or route" in:
        when(see.contains(trainAgent)).thenReturn(false)
        when(ree.contains(trainAgent)).thenReturn(false)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe None

    "return a perception for a train in a station" should:
      "provide a default perception if train hasn't a current timetable" in:
        trainInStationWithTimeTable(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInStation(TrainStationInfo(false, false))
        )

      "provide a default perception if train hasn't a next departure time" in:
        trainInStationWithNextDepartureTime(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInStation(TrainStationInfo(false, false))
        )

      "provide a default perception if train hasn't a next route" in:
        trainInStationWithNextDepartureTime(defaultClockTime)
        when(dtt.nextRoute).thenReturn(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInStation(TrainStationInfo(false, false))
        )

      "provide a default perception if next route is not found in the env" in:
        trainInStationWithNextDepartureTimeAndRouteInfo(defaultClockTime, Seq())
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInStation(TrainStationInfo(true, false))
        )

      "provide a perception when departure time is greater than current time and the direction is unavailable" in:
        trainInStationWithNextDepartureTimeAndRoute(defaultClockTime + ClockTime(0, 1).toOption, false)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInStation(TrainStationInfo(false, false))
        )

      "provide a perception when departure time is greater than current time and the direction is available" in:
        trainInStationWithNextDepartureTimeAndRoute(defaultClockTime + ClockTime(0, 1).toOption, true)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInStation(TrainStationInfo(false, true))
        )

      "provide a perception when departure time is lesser or equal than current time and the direction is unavailable" in:
        trainInStationWithNextDepartureTimeAndRoute(defaultClockTime, false)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInStation(TrainStationInfo(true, false))
        )

      "provide a perception when departure time is lesser or equal than current time and the direction is available" in:
        trainInStationWithNextDepartureTimeAndRoute(defaultClockTime, true)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInStation(TrainStationInfo(true, true))
        )

    "return a perception for a train in a route" should:
      "provide a default perception when hasn't a current timetable" in:
        trainInRouteWithTimeTable(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInRoute(TrainRouteInfo(routeTypology, routeLength, None, false))
        )

      "provide a default perception when hasn't a current route" in:
        trainInRouteWithTimeTable(Some(dtt))
        when(dtt.currentRoute).thenReturn(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInRoute(TrainRouteInfo(routeTypology, routeLength, None, false))
        )

      "provide a default perception when hasn't an arrival station" in:
        trainInRouteWithTimeTable(Some(dtt))
        when(dtt.currentRoute).thenReturn(Some((mockStationA, mockStationB)))
        when(mockStationB.id).thenReturn(1)
        when(see.id).thenReturn(2)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInRoute(TrainRouteInfo(routeTypology, routeLength, None, false))
        )

      "provide a perception when arrival station is not available" in:
        trainInRouteWithStationInfo(false)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInRoute(TrainRouteInfo(routeTypology, routeLength, None, false))
        )

      "provide a perception when arrival station is available" in:
        trainInRouteWithStationInfo(true)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInRoute(TrainRouteInfo(routeTypology, routeLength, None, true))
        )

      "provide a perception when there isn't a train ahead" in:
        trainInRouteWithStationInfoAndTrainAhead(false, 0.0)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInRoute(TrainRouteInfo(routeTypology, routeLength, None, false))
        )

      "provide a perception when there is a train ahead" in:
        val distanceTrainAhead = 10.0
        trainInRouteWithStationInfoAndTrainAhead(false, distanceTrainAhead)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInRoute(TrainRouteInfo(routeTypology, routeLength, Some(distanceTrainAhead), false))
        )

      "provide a perception when there is a train ahead and the arrival station is available" in:
        val distanceTrainAhead = 10.0
        trainInRouteWithStationInfoAndTrainAhead(true, distanceTrainAhead)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(
          TrainPerceptionInRoute(TrainRouteInfo(routeTypology, routeLength, Some(10.0), true))
        )

      "provide a perception when there is a train ahead but the route is note compatible" in:
        val distanceTrainAhead = 10.0
        when(ree.contains(normalTrainAgent)).thenReturn(true)
        when(railwayEnvironment.dynamicTimetableEnvironment.findCurrentTimetableFor(normalTrainAgent)).thenReturn(Some(
          dtt
        ))
        trainInRouteWithStationInfoAndTrainAhead(true, distanceTrainAhead)
        perceptionProvider.perceptionFor(railwayEnvironment, normalTrainAgent) shouldBe Some(
          TrainPerceptionInRoute(TrainRouteInfo(routeTypology, routeLength, None, true))
        )
