package ulisse.entities.simulation

import org.mockito.Mockito.when
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
  private val dt = 1
  private val movement = 10
  private val stationA = Station("A", Coordinate(0, 0), 1)
  private val stationB = Station("B", Coordinate(0, 1), 1)
  private val stationC = Station("C", Coordinate(0, 2), 1)
  private val stationD = Station("D", Coordinate(0, 3), 1)
  private val stations = Seq(stationA, stationB, stationC, stationD)

  given minPermittedDistanceBetweenTrains: Double = 100.0

  private val defaultTechnology = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber = 5
  private val train3905 =
    TrainAgent.apply(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3906 =
    TrainAgent.apply(Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3907 =
    TrainAgent.apply(Train("3907", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val trains = Seq(train3905, train3906, train3907)

  val railsCount: Int = 1
  val typeRoute: TypeRoute = TypeRoute.Normal
  val pathLength: Double = 2 * minPermittedDistanceBetweenTrains + train3905.lengthSize + train3906.lengthSize + train3907.lengthSize
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
      startStation = stationA,
      departureTime = h(20).m(0).getOrDefault
    ).stopsIn(stationB, waitTime = 5)(railAV_10)
     .transitIn(stationC)(railAV_10)
     .arrivesTo(stationD)(railAV_10)

  val timeTable2: Timetable =
    TimetableBuilder(
      train = train3905,
      startStation = stationD,
      departureTime = h(8).m(0).getOrDefault
    ).stopsIn(stationC, waitTime = 5)(railAV_10)
      .transitIn(stationB)(railAV_10)
      .arrivesTo(stationA)(railAV_10)

  val timeTable3: Timetable =
    TimetableBuilder(
      train = train3906,
      startStation = stationA,
      departureTime = h(80).m(0).getOrDefault
    ).stopsIn(stationB, waitTime = 5)(railAV_10)
      .transitIn(stationC)(railAV_10)
      .arrivesTo(stationD)(railAV_10)
  private val timetables = Seq(timeTable1, timeTable2, timeTable3)


  private val env = RailwayEnvironment(
    stations,
    routes,
    trains,
    timetables,
  )

  "RailwayEnvironment" when:
    "created" should:
      "have all stations" in:
        env.stations.map(_.name) should contain theSameElementsAs stations.map(_.name)

      "have all routes" in:
        env.routes should contain theSameElementsAs routes.map(RouteEnvironmentElement(_, minPermittedDistanceBetweenTrains))

      "have all trains associated with a timetable" in:
        val timetablesTrains = timetables.map(_.train).distinct
        env.agents should contain theSameElementsAs trains.filter(timetablesTrains.contains(_)).map(TrainAgent(_))

      "have all timetables" in:
        env.timetables should contain theSameElementsAs timetables.map(DynamicTimetable(_))

      "have placed all trains in their initial stations" in:
        timetables.groupBy(_.train).foreachEntry: (train, timetables) =>
          for
           firstTimetable <- timetables.minByOption(_.departureTime)
           stationEE <- env.stations.find(_.name == firstTimetable.startStation.name)
          yield stationEE.containers.flatMap(_.trains).map(_.name) should contain(train.name)
