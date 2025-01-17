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

  val route: Route = Route(TypeRoute.Normal, path, pathLength, railsCount)
  val routeBank    = RouteManager.fromList(List(route))

  "save new route" should "contains in routeBank" in:
    val routeBank    = RouteManager.empty()
    val newRouteBank = routeBank.save(route)

    routeBank.contains(route) must be(false)
    newRouteBank.contains(route) must be(true)

  "read route" should "from id" in:
    val optRoute = routeBank.route(route.id)
    optRoute match
      case Some(newRoute) => newRoute must be(route)
      case _              => fail("route is empty")

  "save route with same id" should "change route railsCount" in:
    val sameRoute    = Route(TypeRoute.Normal, path, pathLength, 3)
    val newRouteBank = routeBank.save(sameRoute)

    val optRoute     = routeBank.route(route.id)
    val optSameRoute = newRouteBank.route(sameRoute.id)

    optRoute match
      case Some(route) => optSameRoute match
          case Some(sameRoute) =>
            route.railsCount must not be sameRoute.railsCount
          case None => fail("route is empty")
      case None => fail("route is empty")

  "save two different route" should "have two element" in:
    val differentRoute = Route(TypeRoute.AV, path, pathLength, railsCount)
    val newRouteBank   = routeBank.save(differentRoute)

    newRouteBank.contains(differentRoute) must be(true)
    val lengthRouteBank = 2
    newRouteBank.size must be(lengthRouteBank)
