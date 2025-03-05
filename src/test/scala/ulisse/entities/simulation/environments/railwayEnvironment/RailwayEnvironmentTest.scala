package ulisse.entities.simulation.environments.railwayEnvironment

import org.mockito.Mockito.{spy, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.Coordinate
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.RouteEnvironmentElementTest.routeAB
import ulisse.entities.route.RouteTest.departureCoordinate
import ulisse.entities.route.Routes.RouteType.AV
import ulisse.entities.route.Routes.{Route, RouteType}
import ulisse.entities.route.Tracks.TrackDirection
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationDataTest.{
  routesEE,
  stationsEE,
  timetables
}
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.entities.station.Station
import ulisse.entities.station.StationEnvironments.StationEnvironmentElement
import ulisse.entities.station.StationTest.{stationA, stationB}
import ulisse.entities.timetable.DynamicTimetableTest.*
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.timetable.Timetables.{RailInfo, Timetable, TimetableBuilder}
import ulisse.entities.train.TrainAgentTest.{train3905, train3906, train3907}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.utils.Times.FluentDeclaration.h
import ulisse.utils.Times.Time

import scala.Seq

class RailwayEnvironmentTest extends AnyWordSpec with Matchers:
  private val dt = 1

  private final case class FakeTrainAgent(train: Train, distanceTravelled: Double) extends TrainAgent:
    export train.*

    def distanceTravelled_=(newDistanceTravelled: Double): TrainAgent =
      val minDistanceTravelled = 0.0
      copy(distanceTravelled = math.max(minDistanceTravelled, newDistanceTravelled))

    override def doStep(dt: Int, simulationEnvironment: RailwayEnvironment): Option[TrainAgent] =
      Some(copy(distanceTravelled = distanceTravelled + routesEE.map(_.length).foldLeft(0.0)(math.max)))

  private val trainAgent3905 = FakeTrainAgent(train3905, 0.0)
  private val trainAgent3906 = FakeTrainAgent(train3906, 0.0)
  private val trainAgent3907 = FakeTrainAgent(train3907, 0.0)

  private val trainAgents = Seq(trainAgent3905, trainAgent3906, trainAgent3907)

  private val cd = ConfigurationData(
    stationsEE,
    routesEE,
    trainAgents,
    timetables
  )

  private val env = RailwayEnvironment.default(cd)

  extension (env: RailwayEnvironment)
    private def doSteps(steps: Int): RailwayEnvironment =
      (0 until steps).foldLeft(env)((e, _) => e.doStep(dt))

  extension (agent: TrainAgent)
    private def currentInfo(
        env: RailwayEnvironment,
        r: DynamicTimetable => Option[(Station, Station)]
    ): Option[(DynamicTimetable, StationEnvironmentElement, RouteEnvironmentElement, TrackDirection)] =
      for
        currentTT <- env.findCurrentTimeTableFor(agent)
        route     <- r(currentTT)
        _ = println(route)
        see        <- env.stations.find(_ == route._1)
        (ree, dir) <- env.findRouteWithTravelDirection(route)
      yield (currentTT, see, ree, dir)

    private def completeCurrentTimetable(env: RailwayEnvironment): Option[RailwayEnvironment] =
      env.findCurrentTimeTableFor(agent).map(tt => env.doSteps(tt.table.size + 2))

  private def checkConfiguration(env: RailwayEnvironment, cd: ConfigurationData): Unit =
    env.stations shouldBe cd.stations
    env.routes shouldBe cd.routes
    env.timetables shouldBe cd.timetables.values.flatten

  "RailwayEnvironment" when:
    "created" should:
      val time = Time(8, 30, 0)
      val env  = RailwayEnvironment(time, cd)
      "setup initial time" in:
        env.time shouldBe time
      "maintain the configuration" in:
        checkConfiguration(env, cd)

    "created empty" should:
      val env = RailwayEnvironment.empty()
      "setup initial time" in:
        env.time shouldBe Time(0, 0, 0)
      "maintain the configuration" in:
        checkConfiguration(env, ConfigurationData.empty())

    "created default" should:
      val env = RailwayEnvironment.default(cd)
      "setup initial time" in:
        env.time shouldBe Time(0, 0, 0)
      "maintain the configuration" in:
        checkConfiguration(env, cd)

    "doStep" should:
      "move train into route" in:
        trainAgent3905.currentInfo(env.doStep(1), _.currentRoute) match
          case Some(
                tt: DynamicTimetable,
                see: StationEnvironmentElement,
                ree: RouteEnvironmentElement,
                dir: TrackDirection
              ) =>
            tt.stationNr(0).map(_._1).contains(ree.departure) shouldBe true
            tt.stationNr(1).map(_._1).contains(ree.arrival) shouldBe true
            ree.departure shouldBe see
            see.trains.contains(trainAgent3905) shouldBe false
            ree.trains.contains(trainAgent3905) shouldBe true
            ree.containers.find(_.contains(trainAgent3905)) match
              case Some(container) =>
                container.currentDirection shouldBe Some(dir)
                container.trains.find(_ == trainAgent3905).map(_.distanceTravelled) shouldBe Some(0.0)
              case _ => fail()
          case _ => fail()

      "move train into station" in:
        trainAgent3905.currentInfo(env.doSteps(2), _.nextRoute) match
          case Some(
                tt: DynamicTimetable,
                see: StationEnvironmentElement,
                ree: RouteEnvironmentElement,
                dir: TrackDirection
              ) =>
            tt.stationNr(1).map(_._1).contains(ree.departure) shouldBe true
            tt.stationNr(2).map(_._1).contains(ree.arrival) shouldBe true
            ree.departure shouldBe see
            see.trains.contains(trainAgent3905) shouldBe true
            ree.trains.contains(trainAgent3905) shouldBe false
            see.trains.find(_ == trainAgent3905).map(_.distanceTravelled) shouldBe Some(0.0)
          case _ => fail()

      "change schedule" in:
        trainAgent3905.completeCurrentTimetable(env).flatMap(_.findCurrentTimeTableFor(trainAgent3905)) shouldBe Some(
          DynamicTimetable(timetable2)
        )

      "complete schedules" in:
        trainAgent3905.completeCurrentTimetable(env).flatMap(trainAgent3905.completeCurrentTimetable).flatMap(
          _.findCurrentTimeTableFor(trainAgent3905)
        ) shouldBe None
