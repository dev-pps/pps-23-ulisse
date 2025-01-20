package ulisse.applications

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.applications.useCases.RouteManager
import ulisse.entities.Route.{Path, Station, TypeRoute}
import ulisse.entities.{Coordinate, Route}

class RouteManagerTest extends AnyFlatSpec with Matchers:
  val riminiStation: Station = ("Cesena", Coordinate(20d, 20d))
  val cesenaStation: Station = ("Rimini", Coordinate(10d, 10d))
  val path: Path             = (riminiStation, cesenaStation)
  val pathLength: Double     = 200.0d
  val railsCount             = 1
  val route: Route           = Route(TypeRoute.Normal, path, pathLength, railsCount)

  val emptyRouteManager: RouteManager         = RouteManager.empty()
  val singleElementRouteManager: RouteManager = RouteManager.createOf(List(route))

  "create empty routeManager" should "have size 0" in:
    val zeroSize = 0
    emptyRouteManager.size must be(zeroSize)

  "save new route" should "be contains in routeManager" in:
    val newRouteManager = emptyRouteManager.save(route)

    newRouteManager match
      case Left(error) => fail(error.productPrefix)
      case Right(manager) =>
        manager.size must be(1)
        manager.contains(route) must be(true)

  "read route" should "from id" in:
    val optRoute = singleElementRouteManager.route(route.id)
    optRoute match
      case Some(newRoute) => newRoute must be(route)
      case _              => fail("Route not found")

  "save route with same id" should "change route railsCount" in:
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

  "save two different route" should "have two element" in:
    val differentRoute  = Route(TypeRoute.AV, path, pathLength, railsCount)
    val newRouteManager = singleElementRouteManager.save(differentRoute)

    newRouteManager match
      case Left(error) => fail(error.productPrefix)
      case Right(newRouteManager) =>
        val lengthRouteBank = singleElementRouteManager.size + 1
        newRouteManager.contains(differentRoute) must be(true)
        newRouteManager.size must be(lengthRouteBank)
