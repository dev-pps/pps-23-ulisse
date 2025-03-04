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

  private val trainAgent = mock[TrainAgent]
  private val see = mock[StationEnvironmentElement]
  private val ree = mock[RouteEnvironmentElement]
  private val railwayEnvironment = mock[RailwayEnvironment]
  when(railwayEnvironment.stations).thenReturn(Seq(see))
  when(railwayEnvironment.routes).thenReturn(Seq(ree))
  private val perceptionProvider = summon[PerceptionProvider[RailwayEnvironment, TrainAgent]]
  
  private def trainInStation(): Unit =
    when(see.contains(trainAgent)).thenReturn(true)
    when(ree.contains(trainAgent)).thenReturn(false)
  
  private def trainInStationWithTimeTable()
    
  "PerceptionProvider" when:
    "queried for a perception" should:
      "provide a perception for a train in a station" in:
        when(see.contains(trainAgent)).thenReturn(true)
        when(ree.contains(trainAgent)).thenReturn(false)
        when(railwayEnvironment.findCurrentTimeTableFor(trainAgent)).thenReturn(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe a[Some[TrainAgentPerception[TrainStationInfo]]]

      "provide a perception for a train in a route" in:
        when(see.contains(trainAgent)).thenReturn(false)
        when(ree.contains(trainAgent)).thenReturn(true)
        when(ree.containers).thenReturn(Seq())
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
        when(see.contains(trainAgent)).thenReturn(true)
        when(ree.contains(trainAgent)).thenReturn(false)
        when(railwayEnvironment.findCurrentTimeTableFor(trainAgent)).thenReturn(None)
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(false, false)))

      "provide a default perception if train hasn't a next departure time" in:
        when(see.contains(trainAgent)).thenReturn(true)
        when(ree.contains(trainAgent)).thenReturn(false)
        val tt = mock[DynamicTimetable]
        when(tt.nextDepartureTime).thenReturn(None)
        when(railwayEnvironment.findCurrentTimeTableFor(trainAgent)).thenReturn(Some(tt))
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(false, false)))

      "provide a default perception if train hasn't a next route" in:
        when(see.contains(trainAgent)).thenReturn(true)
        when(ree.contains(trainAgent)).thenReturn(false)
        val tt = mock[DynamicTimetable]
        when(tt.nextDepartureTime).thenReturn(ClockTime(0,0).toOption)
        when(railwayEnvironment.time).thenReturn(Time(0,0,0))
        when(tt.nextRoute).thenReturn(None)
        when(railwayEnvironment.findCurrentTimeTableFor(trainAgent)).thenReturn(Some(tt))
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(false, false)))

      "provide a default perception if next route is not found in the env" in:
        when(see.contains(trainAgent)).thenReturn(true)
        when(ree.contains(trainAgent)).thenReturn(false)
        val tt = mock[DynamicTimetable]
        when(tt.nextDepartureTime).thenReturn(ClockTime(0, 0).toOption)
        when(railwayEnvironment.time).thenReturn(Time(0, 0, 0))
        val mockStationA = mock[StationEnvironmentElement]
        val mockStationB = mock[StationEnvironmentElement]
        when(tt.nextRoute).thenReturn(Some((mockStationA, mockStationB)))
        when(railwayEnvironment.findRouteWithTravelDirection((mockStationA, mockStationB))).thenReturn(None)
        when(railwayEnvironment.findCurrentTimeTableFor(trainAgent)).thenReturn(Some(tt))
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(false, false)))

      "provide a perception when departure time is greater than current time and the direction is unavailable" in:
        when(see.contains(trainAgent)).thenReturn(true)
        when(ree.contains(trainAgent)).thenReturn(false)
        val tt = mock[DynamicTimetable]
        when(tt.nextDepartureTime).thenReturn(ClockTime(0, 1).toOption)
        when(railwayEnvironment.time).thenReturn(Time(0, 0, 0))
        val mockStationA = mock[StationEnvironmentElement]
        val mockStationB = mock[StationEnvironmentElement]
        when(tt.nextRoute).thenReturn(Some((mockStationA, mockStationB)))
        when(railwayEnvironment.findRouteWithTravelDirection((mockStationA, mockStationB))).thenReturn(Some((ree, TrackDirection.Forward)))
        when(ree.isAvailable(TrackDirection.Forward)).thenReturn(false)
        when(railwayEnvironment.findCurrentTimeTableFor(trainAgent)).thenReturn(Some(tt))
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(false, false)))

      "provide a perception when departure time is greater than current time and the direction is available" in :
        when(see.contains(trainAgent)).thenReturn(true)
        when(ree.contains(trainAgent)).thenReturn(false)
        val tt = mock[DynamicTimetable]
        when(tt.nextDepartureTime).thenReturn(ClockTime(0, 1).toOption)
        when(railwayEnvironment.time).thenReturn(Time(0, 0, 0))
        val mockStationA = mock[StationEnvironmentElement]
        val mockStationB = mock[StationEnvironmentElement]
        when(tt.nextRoute).thenReturn(Some((mockStationA, mockStationB)))
        when(railwayEnvironment.findRouteWithTravelDirection((mockStationA, mockStationB))).thenReturn(Some((ree, TrackDirection.Forward)))
        when(ree.isAvailable(TrackDirection.Forward)).thenReturn(true)
        when(railwayEnvironment.findCurrentTimeTableFor(trainAgent)).thenReturn(Some(tt))
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(false, true)))

      "provide a perception when departure time is lesser or equal than current time and the direction is unavailable" in :
        when(see.contains(trainAgent)).thenReturn(true)
        when(ree.contains(trainAgent)).thenReturn(false)
        val tt = mock[DynamicTimetable]
        when(tt.nextDepartureTime).thenReturn(ClockTime(0, 0).toOption)
        when(railwayEnvironment.time).thenReturn(Time(0, 0, 0))
        val mockStationA = mock[StationEnvironmentElement]
        val mockStationB = mock[StationEnvironmentElement]
        when(tt.nextRoute).thenReturn(Some((mockStationA, mockStationB)))
        when(railwayEnvironment.findRouteWithTravelDirection((mockStationA, mockStationB))).thenReturn(Some((ree, TrackDirection.Forward)))
        when(ree.isAvailable(TrackDirection.Forward)).thenReturn(false)
        when(railwayEnvironment.findCurrentTimeTableFor(trainAgent)).thenReturn(Some(tt))
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(true, false)))

      "provide a perception when departure time is lesser or equal than current time and the direction is available" in :
        when(see.contains(trainAgent)).thenReturn(true)
        when(ree.contains(trainAgent)).thenReturn(false)
        val tt = mock[DynamicTimetable]
        when(tt.nextDepartureTime).thenReturn(ClockTime(0, 0).toOption)
        when(railwayEnvironment.time).thenReturn(Time(0, 0, 0))
        val mockStationA = mock[StationEnvironmentElement]
        val mockStationB = mock[StationEnvironmentElement]
        when(tt.nextRoute).thenReturn(Some((mockStationA, mockStationB)))
        when(railwayEnvironment.findRouteWithTravelDirection((mockStationA, mockStationB))).thenReturn(Some((ree, TrackDirection.Forward)))
        when(ree.isAvailable(TrackDirection.Forward)).thenReturn(true)
        when(railwayEnvironment.findCurrentTimeTableFor(trainAgent)).thenReturn(Some(tt))
        perceptionProvider.perceptionFor(railwayEnvironment, trainAgent) shouldBe Some(TrainPerceptionInStation(TrainStationInfo(true, true)))