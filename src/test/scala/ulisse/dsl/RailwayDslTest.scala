package ulisse.dsl

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.{must, mustBe}
import org.scalatest.matchers.should.Matchers
import ulisse.applications.AppState
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.dsl.RailwayDsl.*
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes
import ulisse.entities.route.Routes.{Route, RouteType}
import ulisse.entities.station.Station
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

@SuppressWarnings(Array("org.wartremover.warts.Var"))
class RailwayDslTest extends AnyFlatSpec with Matchers:
  private val highSpeed         = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val technologyManager = TechnologyManager(List(highSpeed))
  private val initAppState      = AppState.withTechnology(technologyManager)

  private val platform            = 1
  private val routeLength         = 100.0
  private val departureCoordinate = Coordinate(0, 0)
  private val arrivalCoordinate   = Coordinate(100, 100)
  private val departureName       = "departure"
  private val arrivalName         = "arrival"
  private val trainName           = "test"
  private val departure           = Station(departureName, departureCoordinate, platform)
  private val arrival             = Station(arrivalName, arrivalCoordinate, platform)
  private val trainTest           = Train(trainName, highSpeed, Wagon(UseType.Passenger, platform), platform)
  private val routeTest           = Route(departure, arrival, Routes.RouteType.Normal, platform, routeLength)

  "create station with dsl" should "create a station" in:
    val station = CreateStation -> departureName at (0, 0) platforms platform
    departure mustBe station

  "create train with dsl" should "create a train" in:
    val train = CreateTrain -> trainName technology highSpeed wagon UseType.Passenger capacity platform count 1
    trainTest mustBe train

  "create route with dsl" should "create a route" in:
    val route = CreateRoute -> departure -> arrival on RouteType.Normal tracks platform length routeLength
    route mustBe routeTest

  "create app state with dsl" should "create an app state" in:
    var state = initAppState
    state = CreateAppState || state set departure link routeTest set arrival
    state = CreateAppState || state put trainTest

    state must not be initAppState
    state.stationManager.stations must contain allOf (departure, arrival)
    routeTest.foreach(route => state.routeManager.routes must contain(route))
    state.trainManager.trains must contain(trainTest)

  "create station with route dsl" should "create a station" in:
    var state = initAppState
    state =
      CreateAppState |-> state start departure withType RouteType.Normal tracks platform length routeLength end arrival

    state must not be initAppState
    state.stationManager.stations must contain allOf (departure, arrival)
    routeTest.foreach(route => state.routeManager.routes must contain(route))
