package application

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scala.core.Route
import scala.core.Route.{Path, Station, TypeRoute}
import scala.application.RouteBank

class RouteBankTest extends AnyFlatSpec with Matchers:
  val railsCount             = 1
  val riminiStation: Station = ("Cesena", (20d, 20d))
  val cesenaStation: Station = ("Rimini", (10d, 10d))
  val path: Path             = (riminiStation, cesenaStation)
  val route: Route           = Route(TypeRoute.Normal, railsCount, path)

  "save new route" should "contains in routeBank" in:
    val routeBank    = RouteBank.empty()
    val newRouteBank = routeBank.save(route)

    routeBank.contains(route) must be(false)
    newRouteBank.contains(route) must be(true)

  "read route" should "from id" in:
    val routeBank = RouteBank.fromList(List(route))
    val optRoute  = routeBank.route(route.id)
    optRoute match
      case Some(newRoute) => newRoute must be(route)
      case _              => fail("route is empty")

  "save route with same id" should "change route" in:
    val sameRoute    = Route(TypeRoute.Normal, 3, path)
    val routeBank    = RouteBank.fromList(List(route))
    val newRouteBank = routeBank.save(sameRoute)

    val optRoute     = routeBank.route(route.id)
    val optSameRoute = newRouteBank.route(sameRoute.id)

    optRoute match
      case Some(route) => optSameRoute match
          case Some(sameRoute) =>
            route.railsCount must not be sameRoute.railsCount
          case None => fail("route is empty")
      case None => fail("route is empty")
