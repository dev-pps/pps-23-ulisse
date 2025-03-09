package ulisse

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

class RailwayDslTest extends AnyFlatSpec with Matchers:
  private val appState  = AppState()
  private val highSpeed = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val departure = Station("departure", Coordinate(0, 0), 1)
  private val arrival   = Station("arrival", Coordinate(100, 100), 1)
  private val trainTest = Train("test", highSpeed, Wagon(UseType.Passenger, 1), 1)
  private val routeTest = Route(departure, arrival, Routes.RouteType.Normal, 1, 100.0)

  "create station with dsl" should "create a station" in:
    val station = CreateStation -> "departure" at (0, 0) platforms 1
    departure mustBe station

  "create train with dsl" should "create a train" in:
    val train = CreateTrain -> "test" technology highSpeed wagon UseType.Passenger numbers 1
    trainTest mustBe train

  "create route with dsl" should "create a route" in:
    val route = CreateRoute -> departure -> arrival on RouteType.Normal tracks 1 length 100.0
    route mustBe routeTest

  "create app state with dsl" should "create an app state" in:
    val technologyManager = TechnologyManager(List(highSpeed))
    val appStateWithStation =
      CreateAppState technology (appState, technologyManager) put departure connect routeTest put arrival set trainTest
    appStateWithStation must not be appState

//      CreateDynamicAppState -> departure withType RouteType.Normal withPlatform 1 withLength 100.0 withArrival arrival

//    appStateWithStation mustBe appState.withTechnology(List(highSpeed))
