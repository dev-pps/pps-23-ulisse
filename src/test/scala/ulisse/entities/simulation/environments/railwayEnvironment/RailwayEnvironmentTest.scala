package ulisse.entities.simulation.environments.railwayEnvironment

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Tracks.TrackDirection
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationDataTest.{
  routesEE,
  stationsEE,
  timetables
}
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.timetable.DynamicTimetableTest.*
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.MotionDatas.emptyMotionData
import ulisse.entities.train.TrainAgentTest.{train3905, train3906, train3907}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.TrainAgents.TrainAgent.TrainStates
import ulisse.entities.train.TrainAgents.TrainAgent.TrainStates.{StateBehavior, Stopped}
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.Wagon
import ulisse.utils.Times.Time

import scala.Seq
class RailwayEnvironmentTest extends AnyWordSpec with Matchers:
  private val dt = 1

  private final case class FakeTrainAgent(train: Train, state: StateBehavior) extends TrainAgent:
    export train.*
    export state.motionData
    override def resetDistanceTravelled: TrainAgent =
      FakeTrainAgent(train, state.reset())
    override def updateDistanceTravelled(distanceDelta: Double): TrainAgent =
      this.copy(train, state.withOffsetDistance(distanceDelta))
    override def doStep(dt: Int, simulationEnvironment: RailwayEnvironment): TrainAgent =
      updateDistanceTravelled(routesEE.map(_.length).foldLeft(0.0)(math.max))
    override def distanceTravelled: Double = state.motionData.distanceTravelled

  private val initialState   = Stopped(emptyMotionData)
  private val trainAgent3905 = FakeTrainAgent(train3905, initialState)
  private val trainAgent3906 = FakeTrainAgent(train3906, initialState)
  private val trainAgent3907 = FakeTrainAgent(train3907, initialState)
  private val trainAgents    = Seq(trainAgent3905, trainAgent3906, trainAgent3907)

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
    ): Option[(DynamicTimetable, StationEnvironmentElement, Seq[(RouteEnvironmentElement, TrackDirection)])] =
      for
        currentTT <- env.dynamicTimetableEnvironment.findCurrentTimetableFor(agent)
        route     <- r(currentTT)
        _ = println(route)
        see <- env.stations.find(_ == route._1)
        rd = env.routeEnvironment.findRoutesWithTravelDirection(route)
      yield (currentTT, see, rd)

    private def completeCurrentTimetable(env: RailwayEnvironment): Option[RailwayEnvironment] =
      env.dynamicTimetableEnvironment.findCurrentTimetableFor(agent).map(tt => env.doSteps(tt.table.size + 2))

  private def checkConfiguration(env: RailwayEnvironment, cd: ConfigurationData): Unit =
    env.stations shouldBe cd.stations
    env.routes shouldBe cd.routes
    env.timetables shouldBe cd.timetablesByTrain.values.flatten
    env.dynamicTimetableEnvironment.dynamicTimetablesByTrain shouldBe cd.timetablesByTrain

  private def extractInfo(numStep: Int, r: DynamicTimetable => Option[(Station, Station)]): (DynamicTimetable, StationEnvironmentElement, Seq[(RouteEnvironmentElement, TrackDirection)]) =
    trainAgent3905.currentInfo(env.doSteps(numStep), r) match
      case Some(dtt: DynamicTimetable, see: StationEnvironmentElement, rInfo: Seq[(RouteEnvironmentElement, TrackDirection)]) =>
        print(dtt)
        (dtt, see, rInfo)
      case _ => fail()

  private def matchRouteInfo(d: Station, a: Station, see: StationEnvironmentElement, ree: RouteEnvironmentElement): Unit =
    d shouldBe ree.departure
    a shouldBe ree.arrival
    see shouldBe ree.departure

  "RailwayEnvironment" when:
    "created" should:
      val time = Time(8, 30, 0)
      val env  = RailwayEnvironment(time, cd)
      "setup initial time" in:
        env.time shouldBe time

      "maintain the configuration" in:
        checkConfiguration(env, cd)

    "created auto" should:
      val env = RailwayEnvironment.auto(cd)
      "setup initial time" in:
        env.time.toSeconds shouldBe cd.timetablesByTrain.values.flatten.map(_.departureTime.toSeconds).foldLeft(0)(
          math.min
        )

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
      "update simulation time" in:
        env.doStep(10).time shouldBe Time(0, 0, 10)

      "move train into route" in:
        val (dtt, see, rInfo) = extractInfo(1, _.currentRoute)
        (dtt.stationNr(0).map(_._1), dtt.stationNr(1).map(_._1), rInfo.map(_._1).headOption) match
          case (Some(d), Some(a), Some(ree)) => matchRouteInfo(d, a, see, ree)
          case _ => fail()
        see.trains.contains(trainAgent3905) shouldBe false
        rInfo.map(_._1).collectTrains.contains(trainAgent3905) shouldBe true
        rInfo.find(_._1.contains(trainAgent3905)).map(e => (e._1.containers.find(_.contains(trainAgent3905)), e._2)) match
        case Some(Some(container), dir) =>
          container.currentDirection shouldBe Some(dir)
          container.trains.find(_ == trainAgent3905).map(_.distanceTravelled) shouldBe Some(0.0)
        case _ => fail()

      "move train into station" in:
        val (dtt, see, rInfo) = extractInfo(2, _.nextRoute)
        (dtt.stationNr(1).map(_._1), dtt.stationNr(2).map(_._1), rInfo.map(_._1).headOption) match
          case (Some(d), Some(a), Some(ree)) => matchRouteInfo(d, a, see, ree)
          case _ => fail()
        see.trains.contains(trainAgent3905) shouldBe true
        rInfo.map(_._1).collectTrains.contains(trainAgent3905) shouldBe false
        see.trains.find(_ == trainAgent3905).map(_.distanceTravelled) shouldBe Some(0.0)

      "change schedule" in:
        trainAgent3905.completeCurrentTimetable(env).flatMap(
          _.dynamicTimetableEnvironment.findCurrentTimetableFor(trainAgent3905)
        ) shouldBe Some(
          DynamicTimetable(timetable2)
        )

      "complete schedules" in:
        trainAgent3905.completeCurrentTimetable(env).flatMap(trainAgent3905.completeCurrentTimetable).flatMap(
          _.dynamicTimetableEnvironment.findCurrentTimetableFor(trainAgent3905)
        ) shouldBe None
