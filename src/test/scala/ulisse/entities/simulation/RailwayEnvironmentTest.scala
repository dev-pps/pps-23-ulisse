package ulisse.entities.simulation

import org.mockito.Mockito.{spy, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.Coordinate
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.RouteTest.departureCoordinate
import ulisse.entities.route.Routes.{Route, TypeRoute}
import ulisse.entities.route.Routes.TypeRoute.AV
import ulisse.entities.simulation.Environments.RailwayEnvironment
import ulisse.entities.simulation.Simulations.Actions.MoveBy
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.timetable.DynamicTimetable
import ulisse.entities.timetable.Timetables.{RailInfo, Timetable, TimetableBuilder}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.utils.Times.FluentDeclaration.h

class RailwayEnvironmentTest extends AnyWordSpec with Matchers:
  private val dt       = 1
  private val movement = 10
  private val stationA = Station("A", Coordinate(0, 0), 1)
  private val stationB = Station("B", Coordinate(0, 1), 1)
  private val stationC = Station("C", Coordinate(0, 2), 1)
  private val stationD = Station("D", Coordinate(0, 3), 1)
  private val stations = Seq(stationA, stationB, stationC, stationD)

  given minPermittedDistanceBetweenTrains: Double = 100.0

  private val defaultTechnology  = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon       = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber = 5
  private val train3905 = Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber)
  private val train3906 = Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber)
  private val train3907 = Train("3907", defaultTechnology, defaultWagon, defaultWagonNumber)
  private val trains = Seq(train3905, train3906, train3907)
  private val trainAgent3905 = spy(TrainAgent(train3905))
  private val trainAgent3906 = spy(TrainAgent(train3906))
  private val trainAgent3907 = spy(TrainAgent(train3907))
  private val trainAgents    = Seq(trainAgent3905, trainAgent3906, trainAgent3907)
  val railsCount: Int      = 1
  val typeRoute: TypeRoute = TypeRoute.Normal
  val pathLength: Double =
    trains.foldLeft(2 * minPermittedDistanceBetweenTrains)((length, trainAgent) => length + trainAgent.lengthSize)
  private def routeAB: Route =
    Route(stationA, stationB, typeRoute, railsCount, pathLength) match
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

      "have all routes" in:
        env.routes should contain theSameElementsAs routes.map(RouteEnvironmentElement(
          _,
          minPermittedDistanceBetweenTrains
        ))

      "have at least a subset of all trains associated with a timetable" in:
        val timetablesTrains = timetables.map(_.train).distinct
        trains.filter(timetablesTrains.contains(_)).map(TrainAgent(_)) should contain allElementsOf env.agents

      "have at least a subset of all timetables" in:
        timetables.map(DynamicTimetable(_)) should contain allElementsOf env.timetables

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
            stationWithTrains._2.toList.sortBy(_.name).take(stationEETrains.size).map(_.name) should contain theSameElementsAs stationEETrains
            stationEETrains should have size stationEE.containers.size
            stationEETrains
        env.timetables.map(_.train.name).distinct should contain theSameElementsAs allTrainsInStations.flatten.toList

    "doStep" should:
      "move train" in:
        env.agents.collect({case ta: TrainAgent => ta }).find(_.name == trainAgent3905.name) match
          case Some(train) =>
            env.stations.flatMap(_.containers.flatMap(_.trains)).map(_.name) should contain Seq("3905")
            when(trainAgent3905.doStep(dt, env)).thenReturn(Some(MoveBy(movement)))

//            println(train.doStep(dt, env))
            fail()
          case None => fail()

