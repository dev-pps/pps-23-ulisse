package ulisse.applications.managers

import cats.data.NonEmptyChain
import cats.syntax.either.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.applications.managers.RouteManagers.{Errors, RouteManager}
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes
import ulisse.entities.Routes.{Route, TypeRoute}
import ulisse.entities.station.Station
import ulisse.utils.ValidationUtils.mkStringErrors

class RouteManagerTest extends AnyFlatSpec with Matchers:
  opaque type ValueType        = Double
  opaque type StationTest      = Station[ValueType, Coordinate[ValueType]]
  opaque type RouteTest        = Either[NonEmptyChain[Routes.Errors], Route[ValueType, Coordinate[ValueType]]]
  opaque type RouteManagerTest = RouteManager[ValueType, Coordinate[ValueType]]

  val departure: StationTest = Station("Rimini", Coordinate.createValidRandomGeo(), 2)
  val arrival: StationTest   = Station("Cesena", Coordinate.createValidRandomGeo(), 2)
  val typeRoute: TypeRoute   = TypeRoute.Normal
  val railsCount: Int        = 1
  val pathLength: Double     = departure.coordinate.distance(arrival.coordinate)

  val creationRoute: RouteTest  = Route(departure, arrival, typeRoute, railsCount, pathLength)
  val sameRoute: RouteTest      = Route(departure, arrival, typeRoute, railsCount, pathLength + 100d)
  val differentRoute: RouteTest = Route(departure, arrival, TypeRoute.AV, railsCount, pathLength)

  val emptyRouteManager: RouteManagerTest = RouteManager.empty()
  val singleElementRouteManager: RouteManagerTest =
    creationRoute.map(route => RouteManager.createOf(List(route))).getOrElse(emptyRouteManager)

  "create empty routeManager" should "have size 0" in:
    val zeroSize = 0
    emptyRouteManager.size must be(zeroSize)

  "save new routes in empty route manager" should "be contains in routeManager" in:
    creationRoute.foreach(route =>
      val newRouteManager = emptyRouteManager.save(route)
      newRouteManager match
        case Left(error) => fail(s"${error.msg}")
        case Right(manager) =>
          manager.size must be(emptyRouteManager.size + 1)
          manager.contains(route) must be(true)
    )

  "find route from manager that" should "contains" in:
    creationRoute.foreach(route =>
      singleElementRouteManager.find(route.id) match
        case Right(newRoute) => newRoute must be(route)
        case _               => fail("Route not found")
    )

  "save route with same id" should "launch already exist error" in:
    sameRoute match
      case Left(errors) => fail(errors.mkStringErrors)
      case Right(newRoute) =>
        singleElementRouteManager.save(newRoute) match
          case Left(errors) => errors must be(RouteManagers.Errors.AlreadyExist)
          case _            => fail("Route already exist")

  "save other different routes" should "have two element" in:
    differentRoute match
      case Left(errors) => fail(errors.mkStringErrors)
      case Right(newRoute) =>
        singleElementRouteManager.save(newRoute) match
          case Left(error) => fail(error.msg)
          case Right(newRouteManager) =>
            newRouteManager.size must be(singleElementRouteManager.size + 1)
            newRouteManager.contains(newRoute) must be(true)

  "modify route that not found" should "launch not found error" in:
    for {
      route    <- creationRoute
      newRoute <- differentRoute
    } yield {}

    creationRoute.foreach(route =>
      differentRoute match
        case Left(errors) => fail(errors.mkStringErrors)
        case Right(newRoute) =>
          differentRoute must not be route
          emptyRouteManager.modify(route, newRoute) must be(Errors.NotFound.asLeft[RouteManagerTest])
    )

  "modify route in a route that not exist" should "apply modify" in:
    for {
      route               <- creationRoute
      differentFieldRoute <- sameRoute
    } yield {
      singleElementRouteManager.contains(route) must be(true)
      route.checkAllField(differentFieldRoute) must be(false)
      singleElementRouteManager.modify(route, differentFieldRoute) match
        case Left(error) => fail(s"${error.msg}")
        case Right(newManager) =>
          newManager.size must be(singleElementRouteManager.size)
          newManager.find(differentFieldRoute.id).foreach(_.checkAllField(differentFieldRoute) must be(true))
    }

  "modify route in same of other route" should "launch already exit error" in:
    for {
      differentRoute <- differentRoute
      newRoute       <- creationRoute
    } yield {
      singleElementRouteManager.contains(newRoute) must be(true)
      singleElementRouteManager.save(differentRoute) match
        case Left(errors) => fail(s"${errors.msg}")
        case Right(newManager) =>
          newManager.modify(differentRoute, newRoute) must be(Errors.AlreadyExist.asLeft[RouteManagerTest])
    }

  "delete route that non exist" should "launch not exist error" in:
    for {
      route <- creationRoute
    } yield {
      emptyRouteManager.delete(route.id) must be(Errors.NotExist.asLeft[RouteManagerTest])
    }

  "delete route that exist" should "have size 0" in:
    for {
      route <- creationRoute
    } yield {
      val newRouteManager = singleElementRouteManager.delete(route.id)
      newRouteManager match
        case Left(error) => fail(error.productPrefix)
        case Right(newRouteManager) =>
          newRouteManager.contains(route) must be(false)
          newRouteManager.find(route.id).isLeft must be(true)
          newRouteManager.size must be(singleElementRouteManager.size - 1)
    }
