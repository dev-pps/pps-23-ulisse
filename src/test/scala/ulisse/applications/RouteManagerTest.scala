package ulisse.applications

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.applications.managers.RouteManager
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.Route
import ulisse.entities.Routes.Route.TypeRoute
import ulisse.entities.station.Station

class RouteManagerTest extends AnyFlatSpec with Matchers:
  opaque type ValueType = Double

  val departureStation: Station[ValueType, Coordinate[ValueType]] = Station("Rimini", Coordinate.geo(20d, 20d), 2)
  val arrivalStation: Station[ValueType, Coordinate[ValueType]]   = Station("Cesena", Coordinate.geo(10d, 10d), 2)
  val typeRoute: TypeRoute                                        = TypeRoute.Normal
  val railsCount                                                  = 1
  val pathLength: Double                                          = 200.0d

  val route = Route(departureStation, arrivalStation, typeRoute, railsCount, pathLength)

  val emptyRouteManager         = RouteManager.empty[ValueType, Coordinate[ValueType]]()
  val singleElementRouteManager = RouteManager.createOf[ValueType, Coordinate[ValueType]](List(route))

  "create empty routeManager" should "have size 0" in:
    val zeroSize = 0
    emptyRouteManager.size must be(zeroSize)

  "save new routes" should "be contains in routeManager" in:
    val newRouteManager = emptyRouteManager.save(route)
    newRouteManager match
      case Left(error) => fail(error.productPrefix)
      case Right(manager) =>
        manager.size must be(1)
        manager.contains(route) must be(true)

  "read routes" should "from route" in:
    val optRoute = singleElementRouteManager.find(route.id)
    optRoute match
      case Some(newRoute) => newRoute must be(route)
      case _              => fail("Route not found")

  "save routes with same route" should "change routes railsCount" in:
    val differentRailsCountRoute = Route(departureStation, arrivalStation, typeRoute, railsCount + 1, pathLength)
    val newRouteManager          = singleElementRouteManager.save(differentRailsCountRoute)
    val optOldRoute              = singleElementRouteManager.find(route.id)

    newRouteManager match
      case Left(error) => fail(error.productPrefix)
      case Right(manager) => for {
          oldRoute <- optOldRoute
          newRoute <- manager.find(route.id)
        } yield {
          oldRoute must be(newRoute)
          oldRoute.railsCount must not be newRoute.railsCount
        }

  "save two different routes" should "have two element" in:
    val differentRoute  = Route(departureStation, arrivalStation, TypeRoute.AV, railsCount, pathLength)
    val newRouteManager = singleElementRouteManager.save(differentRoute)

    newRouteManager match
      case Left(error) => fail(error.productPrefix)
      case Right(newRouteManager) =>
        val lengthRouteBank = singleElementRouteManager.size + 1
        newRouteManager.contains(differentRoute) must be(true)
        newRouteManager.size must be(lengthRouteBank)
