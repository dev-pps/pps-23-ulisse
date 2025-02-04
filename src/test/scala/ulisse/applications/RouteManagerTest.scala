package ulisse.applications

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.applications.managers.RouteManager
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Route.{Path, Station, TypeRoute}
import ulisse.entities.Route

class RouteManagerTest extends AnyFlatSpec with Matchers:
  val riminiStation: Station = ("Rimini", Coordinate.geo(20d, 20d))
  val cesenaStation: Station = ("Cesena", Coordinate.geo(10d, 10d))
  val path: Path             = (riminiStation, cesenaStation)
  val pathLength: Double     = 200.0d
  val railsCount             = 1
  val route: Route           = Route(TypeRoute.Normal, path, pathLength, railsCount)

  val emptyRouteManager: RouteManager         = RouteManager.empty()
  val singleElementRouteManager: RouteManager = RouteManager.createOf(List(route))

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

  "read routes" should "from id" in:
    val optRoute = singleElementRouteManager.route(route.id)
    optRoute match
      case Some(newRoute) => newRoute must be(route)
      case _              => fail("Route not found")

  "save routes with same id" should "change routes railsCount" in:
    val differentRailsCountRoute = Route(TypeRoute.Normal, path, pathLength, 3)
    val newRouteManager          = singleElementRouteManager.save(differentRailsCountRoute)
    val optOldRoute              = singleElementRouteManager.route(route.id)

    newRouteManager match
      case Left(error) => fail(error.productPrefix)
      case Right(manager) => for {
          oldRoute <- optOldRoute
          newRoute <- manager.route(route.id)
        } yield {
          oldRoute must be(newRoute)
          oldRoute.railsCount must not be newRoute.railsCount
        }

  "save two different routes" should "have two element" in:
    val differentRoute  = Route(TypeRoute.AV, path, pathLength, railsCount)
    val newRouteManager = singleElementRouteManager.save(differentRoute)

    newRouteManager match
      case Left(error) => fail(error.productPrefix)
      case Right(newRouteManager) =>
        val lengthRouteBank = singleElementRouteManager.size + 1
        newRouteManager.contains(differentRoute) must be(true)
        newRouteManager.size must be(lengthRouteBank)
