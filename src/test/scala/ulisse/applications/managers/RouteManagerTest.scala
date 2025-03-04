package ulisse.applications.managers

import cats.syntax.either.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.applications.managers.RouteManagers.{Errors, RouteManager}
import ulisse.entities.route.RouteTest.*
import ulisse.entities.route.Routes.*

object RouteManagerTest:
  val validateRoute: ValidationRoute          = Route(departure, arrival, typeRoute, railsCount, pathLength)
  val validateEqualRoute: ValidationRoute     = Route(departure, arrival, typeRoute, railsCount, pathLength + 100d)
  val validateDifferentRoute: ValidationRoute = Route(departure, arrival, RouteType.AV, railsCount, pathLength)

  val emptyManager: RouteManager = RouteManager.empty()
  val singleElementManager: RouteManager =
    validateRoute.map(route => RouteManager.createOf(List(route))) getOrElse emptyManager

class RouteManagerTest extends AnyFlatSpec with Matchers:
  import RouteManagerTest.*

  "create empty routeManager" should "have size 0" in:
    val zeroSize = 0
    emptyManager.size mustBe zeroSize

  "check route to test manager" should "be empty" in:
    for
      route          <- validateRoute
      equalRoute     <- validateEqualRoute
      differentRoute <- validateDifferentRoute
    yield
      route mustBe equalRoute
      route must not be differentRoute
      route checkAllField equalRoute mustBe false
      route checkAllField differentRoute mustBe false

  "save new routes in empty route manager" should "be contains in routeManager" in:
    for route <- validateRoute
    yield emptyManager save route match
      case Left(error) => fail(error.msg)
      case Right(manager) =>
        manager.size mustBe (emptyManager.size + 1)
        manager contains route mustBe true

  "find route from manager that" should "contains" in:
    for route <- validateRoute
    yield
      singleElementManager findBy route.id mustBe (singleElementManager find route)
      singleElementManager findBy route.id match
        case Right(newRoute) => newRoute mustBe route
        case _               => fail("Route not found")

  "save equal route" should "launch already exist error" in:
    for equalRoute <- validateEqualRoute
    yield singleElementManager save equalRoute match
      case Left(errors) => errors mustBe RouteManagers.Errors.AlreadyExist
      case _            => fail("Route already exist")

  "save other different routes" should "have two element" in:
    for differentRoute <- validateDifferentRoute
    yield singleElementManager save differentRoute match
      case Left(error) => fail(error.msg)
      case Right(newRouteManager) =>
        newRouteManager.size mustBe (singleElementManager.size + 1)
        newRouteManager contains differentRoute mustBe true

  "modify route that not found" should "launch not found error" in:
    for
      route          <- validateRoute
      differentRoute <- validateDifferentRoute
    yield emptyManager modify (route, differentRoute) mustBe Errors.NotFound.asLeft[RouteManager]

  "modify route in a route that not exist" should "apply modify" in:
    for
      route      <- validateRoute
      equalRoute <- validateEqualRoute
    yield singleElementManager modify (route, equalRoute) match
      case Left(error) => fail(error.msg)
      case Right(newManager) =>
        newManager.size mustBe singleElementManager.size
        newManager find equalRoute match
          case Left(error)      => fail(error.msg)
          case Right(findRoute) => findRoute checkAllField equalRoute mustBe true

  "modify route in equals of a other route" should "launch already exit error" in:
    for
      route          <- validateRoute
      differentRoute <- validateDifferentRoute
    yield singleElementManager save differentRoute match
      case Left(errors) => fail(errors.msg)
      case Right(newManager) =>
        newManager modify (differentRoute, route) mustBe Errors.AlreadyExist.asLeft[RouteManager]

  "delete route that non exist" should "launch not exist error" in:
    for route <- validateRoute
    yield
      val newManager = emptyManager deleteBy route.id
      newManager mustBe emptyManager.delete(route)
      newManager mustBe emptyManager

  "delete route that exist" should "have size 0" in:
    for route <- validateRoute
    yield
      val newManager = singleElementManager deleteBy route.id
      newManager.size mustBe (singleElementManager.size - 1)
      newManager mustBe (singleElementManager delete route)
      newManager contains route mustBe false

  "find route from departure station" should "be contains in routeManager" in:
    for route <- validateRoute
    yield singleElementManager findByDeparture route.departure mustBe List(route)

  "find route from departure station that not exist" should "be empty" in:
    for route <- validateRoute
    yield singleElementManager findByDeparture route.arrival mustBe List.empty[Route]

  "find route from arrival station" should "be contains in routeManager" in:
    for route <- validateRoute
    yield singleElementManager findByArrival route.arrival mustBe List(route)

  "find route from arrival station that not exist" should "be empty" in:
    for route <- validateRoute
    yield singleElementManager findByArrival route.departure mustBe List.empty[Route]

  "find route from path" should "be contains in routeManager" in:
    for route <- validateRoute
    yield
      singleElementManager findByPath (route.departure, route.arrival) mustBe List(route)
      singleElementManager findByPath (route.arrival, route.departure) mustBe List(route)

  "delete route from departure station" should "be empty" in:
    for route <- validateRoute
    yield singleElementManager deleteByDeparture departure mustBe emptyManager

  "delete route form departure station that not exist" should "be empty" in:
    for route <- validateRoute
    yield singleElementManager deleteByDeparture arrival mustBe singleElementManager

  "delete route from arrival station" should "be empty" in:
    for route <- validateRoute
    yield singleElementManager deleteByArrival arrival mustBe emptyManager

  "delete route form arrival station that not exist" should "be empty" in:
    for route <- validateRoute
    yield singleElementManager deleteByArrival departure mustBe singleElementManager
