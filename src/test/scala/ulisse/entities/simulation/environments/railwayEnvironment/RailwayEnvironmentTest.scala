package ulisse.entities.simulation.environments.railwayEnvironment

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Tracks.TrackDirection
import ulisse.entities.simulation.environments.railwayEnvironment
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationDataTest.{
  routesEE,
  stationsEE,
  timetables
}
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.timetable.DynamicTimetableTest.*
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.MotionDatas.emptyMotionData
import ulisse.entities.train.TrainAgentStates.{StateBehavior, Stopped}
import ulisse.entities.train.TrainAgentTest.{train3905, train3906, train3907}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.Wagon
import ulisse.utils.Times.Time

import scala.Seq
class RailwayEnvironmentTest extends AnyWordSpec with Matchers:
  private val dt = 1

  private final case class FakeTrainAgent(train: Train, state: StateBehavior, stepDistance: Int) extends TrainAgent:
    export train.*
    export state.motionData
    override def resetDistanceTravelled: TrainAgent =
      copy(state = state.reset())
    override def updateDistanceTravelled(distanceDelta: Double): TrainAgent =
      copy(state = state.withOffsetDistance(distanceDelta))
    override def doStep(dt: Int, simulationEnvironment: RailwayEnvironment): TrainAgent =
      updateDistanceTravelled(stepDistance)
    override def distanceTravelled: Double = state.motionData.distanceTravelled

  private val initialState        = Stopped(emptyMotionData)
  private val defaultStepDistance = routesEE.map(_.length).foldLeft(0.0)(math.max).ceil.toInt
  private val trainAgent3905      = FakeTrainAgent(train3905, initialState, defaultStepDistance)
  private val trainAgent3906      = FakeTrainAgent(train3906, initialState, 0)
  private val trainAgent3907      = FakeTrainAgent(train3907, initialState, defaultStepDistance)
  private val trainAgents         = Seq(trainAgent3905, trainAgent3906, trainAgent3907)

  private val cd = ConfigurationData(
    stationsEE,
    routesEE,
    trainAgents,
    timetables
  )

  private given env: RailwayEnvironment = RailwayEnvironment.default(cd)

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
        see       <- env.stations.find(_ == route._1)
        rd = env.routeEnvironment.findRoutesWithTravelDirection(route)
      yield (currentTT, see, rd)

    private def completeCurrentTimetable(env: RailwayEnvironment): Option[RailwayEnvironment] =
      env.dynamicTimetableEnvironment.findCurrentTimetableFor(agent).map(tt => env.doSteps(tt.table.size + 2))

    private def trackWithDirectionFromRouteInfo(rInfo: Seq[(RouteEnvironmentElement, TrackDirection)])
        : Option[(Option[RouteEnvironmentElement#TAC], TrackDirection)] =
      rInfo.find(_._1.contains(trainAgent3905)).map(e => (e._1.containers.find(_.contains(trainAgent3905)), e._2))

  private def checkConfiguration(env: RailwayEnvironment, cd: ConfigurationData): Unit =
    env.stations shouldBe cd.stations
    env.routes shouldBe cd.routes
    env.timetables shouldBe cd.timetablesByTrain.values.flatten
    env.dynamicTimetableEnvironment.dynamicTimetablesByTrain shouldBe cd.timetablesByTrain

  extension (trainAgent: TrainAgent)
    private def extractInfo(
        numStep: Int,
        r: DynamicTimetable => Option[(Station, Station)]
    )(using
        env: RailwayEnvironment
    ): (DynamicTimetable, StationEnvironmentElement, Seq[(RouteEnvironmentElement, TrackDirection)]) =
      trainAgent.currentInfo(env.doSteps(numStep), r) match
        case Some(
              dtt: DynamicTimetable,
              see: StationEnvironmentElement,
              rInfo: Seq[(RouteEnvironmentElement, TrackDirection)]
            ) =>
          (dtt, see, rInfo)
        case _ => fail()

  private def matchRouteInfo(
      d: Station,
      a: Station,
      see: StationEnvironmentElement,
      ree: RouteEnvironmentElement
  ): Unit =
    d shouldBe ree.departure
    a shouldBe ree.arrival
    see shouldBe ree.departure

  def validateRoute(
      dtt: DynamicTimetable,
      startPosition: Int,
      rInfo: Seq[(RouteEnvironmentElement, TrackDirection)],
      see: StationEnvironmentElement
  ): Unit =
    (
      dtt.stationNr(startPosition).map(_._1),
      dtt.stationNr(startPosition + 1).map(_._1),
      rInfo.map(_._1).headOption
    ) match
      case (Some(d), Some(a), Some(ree)) => matchRouteInfo(d, a, see, ree)
      case _                             => fail()

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
        env.time shouldBe cd.departureTime

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
        val (dtt, see, rInfo) = trainAgent3905.extractInfo(1, _.currentRoute)
        validateRoute(dtt, 0, rInfo, see)
        see.trains.contains(trainAgent3905) shouldBe false
        rInfo.map(_._1).collectTrains.find(_ == trainAgent3905).map(_.distanceTravelled) shouldBe Some(0.0)
        trainAgent3905.trackWithDirectionFromRouteInfo(rInfo) match
          case Some(Some(container), dir) => container.currentDirection shouldBe Some(dir)
          case _                          => fail()

      "move train into station" in:
        val (dtt, see, rInfo) = trainAgent3905.extractInfo(2, _.nextRoute)
        validateRoute(dtt, 1, rInfo, see)
        see.trains.contains(trainAgent3905) shouldBe true
        rInfo.map(_._1).collectTrains.contains(trainAgent3905) shouldBe false
        see.trains.find(_ == trainAgent3905).map(_.distanceTravelled) shouldBe Some(0.0)

      "do not move train in a route if it has not moved" in:
        val (dtt, see, rInfo) = trainAgent3906.extractInfo(1, _.nextRoute)
        see.trains.contains(trainAgent3906) shouldBe true
        rInfo.map(_._1).collectTrains.contains(trainAgent3906) shouldBe false
        see.trains.find(_ == trainAgent3906).map(_.distanceTravelled) shouldBe Some(0.0)

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
