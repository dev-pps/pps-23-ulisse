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

  "save same route" should "not contains in routeBank" in:
    val sameRoute    = Route(TypeRoute.Normal, 3, path)
    val routeBank    = RouteBank(List(route))
    val newRouteBank = routeBank.save(sameRoute)

//    newRouteBank.contains(sameRoute) must be(false)
