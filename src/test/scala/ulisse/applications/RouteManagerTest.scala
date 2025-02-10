package ulisse.applications

import cats.syntax.either.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.applications.managers.RouteManagers.{Errors, RouteManager}
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.Route
import ulisse.entities.Routes.Route.TypeRoute
import ulisse.entities.station.Station

class RouteManagerTest extends AnyFlatSpec with Matchers:
  opaque type ValueType        = Double
  opaque type StationTest      = Station[ValueType, Coordinate[ValueType]]
  opaque type RouteTest        = Route[ValueType, Coordinate[ValueType]]
  opaque type RouteManagerTest = RouteManager[ValueType, Coordinate[ValueType]]

  val departureStation: StationTest = Station("Rimini", Coordinate.createValidRandomGeo(), 2)
  val arrivalStation: StationTest   = Station("Cesena", Coordinate.createValidRandomGeo(), 2)
  val typeRoute: TypeRoute          = TypeRoute.Normal
  val railsCount: Int               = 1
  val pathLength: Double            = 200.0d

  val route: RouteTest = Route(departureStation, arrivalStation, typeRoute, railsCount, pathLength)

  val emptyRouteManager: RouteManagerTest         = RouteManager.empty()
  val singleElementRouteManager: RouteManagerTest = RouteManager.createOf(List(route))

  "create empty routeManager" should "have size 0" in:
    val zeroSize = 0
    emptyRouteManager.size must be(zeroSize)

  "save new routes in empty route manager" should "be contains in routeManager" in:
    val newRouteManager = emptyRouteManager.save(route)
    newRouteManager match
      case Left(error) => fail(error.productPrefix)
      case Right(manager) =>
        manager.size must be(emptyRouteManager.size + 1)
        manager.contains(route) must be(true)

  "find route from manager that" should "contains" in:
    val optRoute = singleElementRouteManager.find(route.id)
    optRoute match
      case Some(newRoute) => newRoute must be(route)
      case _              => fail("Route not found")

  "save route with same id" should "launch already exist error" in:
    val differentRailsCountRoute = Route(departureStation, arrivalStation, typeRoute, railsCount, pathLength)
    val newRouteManager          = singleElementRouteManager.save(differentRailsCountRoute)
    val error: Either[Errors, RouteManagerTest] = Errors.AlreadyExist.asLeft

    route must be(differentRailsCountRoute)
    newRouteManager must be(error)

  "save other different routes" should "have two element" in:
    val differentRoute  = Route(departureStation, arrivalStation, TypeRoute.AV, railsCount, pathLength)
    val newRouteManager = singleElementRouteManager.save(differentRoute)

    route must not be differentRoute
    newRouteManager match
      case Left(error) => fail(error.productPrefix)
      case Right(newRouteManager) =>
        newRouteManager.size must be(singleElementRouteManager.size + 1)
        newRouteManager.contains(differentRoute) must be(true)

  "modify route that non exist" should "launch not found error" in:
    val newRouteManager                         = emptyRouteManager.modify(route)
    val error: Either[Errors, RouteManagerTest] = Errors.NotFound.asLeft

    newRouteManager must be(error)

  "modify route that exist" should "have new route" in:
    val newRoute        = Route(departureStation, arrivalStation, typeRoute, railsCount + 1, pathLength)
    val newRouteManager = singleElementRouteManager.modify(route)

    route must be(newRoute)
    newRouteManager match
      case Left(error)            => fail(error.productPrefix)
      case Right(newRouteManager) => newRouteManager.find(newRoute.id) must be(Some(newRoute))

  "delete route that non exist" should "launch not exist error" in:
    val newRouteManager                         = emptyRouteManager.delete(route.id)
    val error: Either[Errors, RouteManagerTest] = Errors.NotExist.asLeft

    newRouteManager must be(error)

  "delete route that exist" should "have size 0" in:
    val newRouteManager = singleElementRouteManager.delete(route.id)

    newRouteManager match
      case Left(error) => fail(error.productPrefix)
      case Right(newRouteManager) =>
        newRouteManager.contains(route) must be(false)
        newRouteManager.find(route.id).isEmpty must be(true)
        newRouteManager.size must be(singleElementRouteManager.size - 1)
