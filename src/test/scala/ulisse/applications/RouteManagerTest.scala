package ulisse.applications

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.applications.useCases.RouteManager
import ulisse.entities.Route.{Path, Station, TypeRoute}
import ulisse.entities.{Coordinate, Route}

class RouteManagerTest extends AnyFlatSpec with Matchers:
  val riminiStation: Station = ("Cesena", Coordinate.createGeo(20d, 20d))
  val cesenaStation: Station = ("Rimini", Coordinate.createGeo(10d, 10d))
  val path: Path             = (riminiStation, cesenaStation)
  val pathLength: Double     = 200.0d
  val railsCount             = 1

  val route: Route               = Route(TypeRoute.Normal, path, pathLength, railsCount)
  val routeManager: RouteManager = RouteManager.fromList(List(route))

  "create empty routeManager" should "have size 0" in:
    val routeBank = RouteManager.empty()
    routeBank.size must be(0)

  "save new route" should "contains in routeBank" in:
    val routeManager    = RouteManager.empty()
    val newRouteManager = routeManager.save(route)

    newRouteManager match
      case Left(error) => fail(error.productPrefix)
      case Right(newRouteManager) =>
        routeManager.contains(route) must be(false)
        newRouteManager.contains(route) must be(true)

  "read route" should "from id" in:
    val optRoute = routeManager.route(route.id)
    optRoute match
      case Some(newRoute) => newRoute must be(route)
      case _              => fail("route is empty")

  "save route with same id" should "change route railsCount" in:
    val differentRailsCountRoute = Route(TypeRoute.Normal, path, pathLength, 3)
    val newRouteManager          = routeManager.save(differentRailsCountRoute)
    val optOldRoute              = routeManager.route(route.id)

    newRouteManager match
      case Left(error) => fail(error.productPrefix)
      case Right(newRouteManager) => newRouteManager.route(differentRailsCountRoute.id) match
          case Some(route) =>
            route.railsCount must be(differentRailsCountRoute.railsCount)
            optOldRoute.map(oldRout => route.railsCount must not be oldRout.railsCount)
          case None => fail("Route not found")

  "save two different route" should "have two element" in:
    val differentRoute  = Route(TypeRoute.AV, path, pathLength, railsCount)
    val newRouteManager = routeManager.save(differentRoute)

    newRouteManager match
      case Left(error) => fail(error.productPrefix)
      case Right(newRouteManager) =>
        val lengthRouteBank = routeManager.size + 1
        newRouteManager.contains(differentRoute) must be(true)
        newRouteManager.size must be(lengthRouteBank)
