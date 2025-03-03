package ulisse.entities.simulation.environments

import org.mockito.Mockito.{spy, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.Coordinate
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.RouteTest.departureCoordinate
import ulisse.entities.route.Routes.TypeRoute.AV
import ulisse.entities.route.Routes.{Route, TypeRoute}
import ulisse.entities.simulation.environments.RailwayEnvironment
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.timetable.Timetables.{RailInfo, Timetable, TimetableBuilder}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.utils.Times.FluentDeclaration.h

import scala.Seq

class RailwayEnvironmentTest extends AnyWordSpec with Matchers:
  private val dt        = 1
  private val movement  = 10
  private val stationA  = Station("A", Coordinate(0, 0), 1)
  private val stationA2 = Station("A", Coordinate(0, 0), 3)
  private val stationB  = Station("B", Coordinate(0, 1), 1)
  private val stationC  = Station("C", Coordinate(0, 2), 1)
  private val stationD  = Station("D", Coordinate(0, 3), 1)
  private val stations  = Seq(stationA, stationB, stationC, stationD)

  given minPermittedDistanceBetweenTrains: Double = 100.0

  private val defaultTechnology  = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon       = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber = 5
  private val train3905          = Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber)
  private val train39052         = Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber + 1)
  private val train3906          = Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber)
  private val train3907          = Train("3907", defaultTechnology, defaultWagon, defaultWagonNumber)
  private val trains             = Seq(train3905, train3906, train3907)

  private final case class FakeTrainAgent(train: Train, distanceTravelled: Double) extends TrainAgent:
    export train.*

    def distanceTravelled_=(newDistanceTravelled: Double): TrainAgent =
      val minDistanceTravelled = 0.0
      copy(distanceTravelled = math.max(minDistanceTravelled, newDistanceTravelled))

    override def doStep(dt: Int, simulationEnvironment: RailwayEnvironment): Option[TrainAgent] =
      Some(copy(distanceTravelled = distanceTravelled + pathLength))
  private val trainAgent3905  = FakeTrainAgent(train3905, 0.0)
  private val trainAgent39052 = FakeTrainAgent(train39052, 0.0)
  private val trainAgent3906  = FakeTrainAgent(train3906, 0.0)
  private val trainAgent3907  = FakeTrainAgent(train3907, 0.0)
  private def mocked(train: Train): TrainAgent =
    val trainAgent = mock[TrainAgent]
    when(trainAgent.name).thenReturn(train.name)
    when(trainAgent.length).thenReturn(train.length)
    when(trainAgent.wagon).thenReturn(train.wagon)
    when(trainAgent.maxSpeed).thenReturn(train.maxSpeed)
    when(trainAgent.lengthSize).thenReturn(train.lengthSize)
    trainAgent
  private val trainAgents  = Seq(trainAgent3905, trainAgent3906, trainAgent3907)
  val railsCount: Int      = 1
  val typeRoute: TypeRoute = TypeRoute.Normal
  val pathLength: Double =
    trains.foldLeft(2 * minPermittedDistanceBetweenTrains)((length, trainAgent) => length + trainAgent.lengthSize)
  private val routeAB: Route =
    Route(stationA, stationB, typeRoute, railsCount, pathLength) match
      case Left(errors) => fail()
      case Right(route) => route
  private val routeAB2: Route =
    Route(stationA, stationB, typeRoute, railsCount, pathLength + 1) match
      case Left(errors) => fail()
      case Right(route) => route
  private val routeBC: Route =
    Route(stationB, stationC, typeRoute, railsCount, pathLength) match
      case Left(errors) => fail()
      case Right(route) => route
  private val routeCD: Route =
    Route(stationC, stationD, typeRoute, railsCount, pathLength) match
      case Left(errors) => fail()
      case Right(route) => route
  private val routeDA: Route =
    Route(stationD, stationA, typeRoute, railsCount, pathLength) match
      case Left(errors) => fail()
      case Right(route) => route
  private val routes = Seq(routeAB, routeBC, routeCD, routeDA)

  val railAV_10: RailInfo = RailInfo(length = 10, typeRoute = AV)

  val timeTable1: Timetable =
    TimetableBuilder(
      train = train3905,
      startStation = stationD,
      departureTime = h(20).m(0).getOrDefault
    ).stopsIn(stationC, waitTime = 5)(railAV_10)
      .transitIn(stationB)(railAV_10)
      .arrivesTo(stationA)(railAV_10)

  val timeTable2: Timetable =
    TimetableBuilder(
      train = train3905,
      startStation = stationA,
      departureTime = h(8).m(0).getOrDefault
    ).stopsIn(stationB, waitTime = 5)(railAV_10)
      .transitIn(stationC)(railAV_10)
      .arrivesTo(stationD)(railAV_10)

  val timeTable22: Timetable =
    TimetableBuilder(
      train = train39052,
      startStation = stationA,
      departureTime = h(8).m(0).getOrDefault
    ).stopsIn(stationB, waitTime = 5)(railAV_10)
      .transitIn(stationC)(railAV_10)
      .arrivesTo(stationD)(railAV_10)

  val timeTable3: Timetable =
    TimetableBuilder(
      train = train3906,
      startStation = stationA,
      departureTime = h(8).m(0).getOrDefault
    ).stopsIn(stationB, waitTime = 5)(railAV_10)
      .transitIn(stationC)(railAV_10)
      .arrivesTo(stationD)(railAV_10)

  private val timetables = Seq(timeTable1, timeTable2, timeTable3)

  private val env = RailwayEnvironment(
    stations.map(StationEnvironmentElement(_)),
    routes.map(RouteEnvironmentElement(_, minPermittedDistanceBetweenTrains)),
    trainAgents,
    timetables.map(DynamicTimetable(_))
  )

  "RailwayEnvironment" when:
    "created" should:
      "have all stations" in:
        env.stations.map(_.name) should contain theSameElementsAs stations.map(_.name)

      "exclude in order duplicate stations" in:
        val env = RailwayEnvironment(
          (stations :+ stationA2).map(StationEnvironmentElement(_)),
          routes.map(RouteEnvironmentElement(_, minPermittedDistanceBetweenTrains)),
          trainAgents,
          timetables.map(DynamicTimetable(_))
        )
        env.stations.map(s => (s.name, s.numberOfTracks)) should contain theSameElementsAs stations.map(s =>
          (s.name, s.numberOfTracks)
        )

      "have all routes" in:
        env.routes shouldBe routes.map(RouteEnvironmentElement(
          _,
          minPermittedDistanceBetweenTrains
        ))

      "exclude in order duplicate routes" in:
        val env = RailwayEnvironment(
          stations.map(StationEnvironmentElement(_)),
          (routes :+ routeAB2).map(RouteEnvironmentElement(_, minPermittedDistanceBetweenTrains)),
          trainAgents,
          timetables.map(DynamicTimetable(_))
        )
        env.routes shouldBe routes.map(RouteEnvironmentElement(
          _,
          minPermittedDistanceBetweenTrains
        ))

      "have at least a subset of all trains associated with a timetable" in:
        val timetablesTrains = timetables.map(_.train).distinct
        trains.filter(timetablesTrains.contains(_)).map(FakeTrainAgent(_, 0.0)) should contain allElementsOf env.agents

      "exclude in order duplicate trains" in:
        val env = RailwayEnvironment(
          stations.map(StationEnvironmentElement(_)),
          routes.map(RouteEnvironmentElement(_, minPermittedDistanceBetweenTrains)),
          (trainAgents :+ trainAgent39052),
          timetables.map(DynamicTimetable(_))
        )
        val trains = env.agents.collect({ case t: TrainAgent => (t.name, t.length) })
        trains.filter(_._1 == train3905.name) shouldBe Seq((trainAgent3905.name, defaultWagonNumber))

      "have at least a subset of all timetables" in:
        timetables.map(DynamicTimetable(_)) should contain allElementsOf env.timetables

      "exclude in order duplicate timetables" in:
        val env = RailwayEnvironment(
          stations.map(StationEnvironmentElement(_)),
          routes.map(RouteEnvironmentElement(_, minPermittedDistanceBetweenTrains)),
          trainAgents,
          (timetables :+ timeTable22).map(DynamicTimetable(_))
        )
        env.timetables should contain allElementsOf Seq(DynamicTimetable(timeTable2))
        env.timetables should not contain DynamicTimetable(timeTable22)

      "have placed the trains in their initial stations if possible and then for the others drops it with all their time tables" in:
        val trainWithFirstDepartureStation =
          for
            a <- timetables.groupBy(_.train).flatMap(_._2.minByOption(_.departureTime)).map(tt =>
              (tt.train, tt.startStation)
            )
            b <- stations.find(_.name == a._2.name)
          yield (a._1, b)

        val allTrainsInStations =
          for
            stationWithTrains <- trainWithFirstDepartureStation.groupBy(_._2).view.mapValues(_.map(_._1))
            stationEE         <- env.stations.find(_.name == stationWithTrains._1.name)
          yield
            val stationEETrains = stationEE.containers.flatMap(_.trains).map(_.name)
            stationWithTrains._2.toList.sortBy(_.name).take(stationEETrains.size).map(
              _.name
            ) should contain theSameElementsAs stationEETrains
            stationEETrains should have size stationEE.containers.size
            stationEETrains
        env.timetables.map(_.train.name).distinct should contain theSameElementsAs allTrainsInStations.flatten.toList

    "doStep" should:
      "move train into route" in:
        env.trains.find(_.name == trainAgent3905.name) match
          case Some(train) =>
            env.stations.flatMap(_.containers.flatMap(_.trains)).map(_.name).contains(trainAgent3905.name) shouldBe true
            val newEnv = env.doStep(dt)
            (newEnv.stations.find(_.name == stationA.name), newEnv.routes.find(_.id == routeAB.id)) match
              case (Some(stationEE), Some(routeEE)) =>
                stationEE.containers.flatMap(_.trains).map(_.name).contains(trainAgent3905.name) shouldBe false
                val updatedAgent = routeEE.containers.flatMap(_.trains).find(_.name == trainAgent3905.name)
                updatedAgent shouldBe defined
                newEnv.findCurrentTimeTableFor(trainAgent3905).flatMap(_.currentRoute).flatMap(
                  newEnv.findRouteWithTravelDirection
                ).flatMap((ree, dir) =>
                  routeEE.containers.find(_.contains(trainAgent3905)).flatMap(_.currentDirection).map(_ == dir)
                ) shouldBe Some(true)
                updatedAgent.map(_.distanceTravelled) shouldBe Some(0.0)
              case _ => fail()
          case None => fail()

      "move train into station" in:
        env.trains.find(_.name == trainAgent3905.name) match
          case Some(train) =>
            val newEnv = env.doStep(dt).doStep(dt)
            (newEnv.stations.find(_.name == stationB.name), newEnv.routes.find(_.id == routeAB.id)) match
              case (Some(stationEE), Some(routeEE)) =>
                println(stationEE.containers.flatMap(_.trains).map(_.name))
                val updatedAgent = stationEE.containers.flatMap(_.trains).find(_.name == trainAgent3905.name)
                updatedAgent shouldBe defined
                updatedAgent.map(_.distanceTravelled) shouldBe Some(0.0)
                routeEE.containers.flatMap(_.trains).map(_.name).contains(trainAgent3905.name) shouldBe false
                routeEE.containers.foreach(_.currentDirection shouldBe None)
              case _ => fail()
          case None => fail()

      "change schedule" in:
        env.agents.collect({ case ta: TrainAgent => ta }).find(_.name == trainAgent3905.name) match
          case Some(train) =>
            val newEnv0 = env.doStep(dt).doStep(dt).doStep(dt).doStep(dt).doStep(dt)
            println("lastStep")
            val newEnv = newEnv0.doStep(dt)
            val ctt = newEnv.findCurrentTimeTableFor(trainAgent3905).map(ctt =>
              println(ctt.table)
              println(ctt.effectiveTable)
              println(ctt.currentRoute)
              println(ctt.nextRoute)
              println(ctt.completed)
              ctt
            )
            newEnv.findCurrentTimeTableFor(trainAgent3905) shouldBe Some(DynamicTimetable(timeTable1))
          case _ => fail()

      "complete schedules" in:
        val newEnv = env.doStep(dt).doStep(dt).doStep(dt).doStep(dt).doStep(dt).doStep(dt).doStep(dt).doStep(dt).doStep(
          dt
        ).doStep(dt).doStep(dt).doStep(dt)
        newEnv.findCurrentTimeTableFor(trainAgent3905) shouldBe None
