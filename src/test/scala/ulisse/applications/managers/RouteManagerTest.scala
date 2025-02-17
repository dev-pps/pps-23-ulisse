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
  val validateDifferentRoute: ValidationRoute = Route(departure, arrival, TypeRoute.AV, railsCount, pathLength)

  val emptyManager: RouteManager = RouteManager.empty()
  val singleElementManager: RouteManager =
    validateRoute.map(route => RouteManager.createOf(List(route))).getOrElse(emptyManager)

class RouteManagerTest extends AnyFlatSpec with Matchers:
  import RouteManagerTest.*

  "create empty routeManager" should "have size 0" in:
    val zeroSize = 0
    emptyManager.size must be(zeroSize)

  "check route to test manager" should "be empty" in:
    for
      route          <- validateRoute
      equalRoute     <- validateEqualRoute
      differentRoute <- validateDifferentRoute
    yield
      route must be(equalRoute)
      route must not be differentRoute
      route.checkAllField(equalRoute) must be(false)
      route.checkAllField(differentRoute) must be(false)

  "save new routes in empty route manager" should "be contains in routeManager" in:
    for
      route <- validateRoute
    yield emptyManager.save(route) match
      case Left(error) => fail(s"${error.msg}")
      case Right(manager) =>
        manager.size must be(emptyManager.size + 1)
        manager.contains(route) must be(true)

  "find route from manager that" should "contains" in:
    for
      route <- validateRoute
    yield
      singleElementManager.findBy(route.id) must be(singleElementManager.find(route))
      singleElementManager.findBy(route.id) match
        case Right(newRoute) => newRoute must be(route)
        case _               => fail("Route not found")

  "save equal route" should "launch already exist error" in:
    for
      equalRoute <- validateEqualRoute
    yield singleElementManager.save(equalRoute) match
      case Left(errors) => errors must be(RouteManagers.Errors.AlreadyExist)
      case _            => fail("Route already exist")

  "save other different routes" should "have two element" in:
    for
      differentRoute <- validateDifferentRoute
    yield singleElementManager.save(differentRoute) match
      case Left(error) => fail(error.msg)
      case Right(newRouteManager) =>
        newRouteManager.size must be(singleElementManager.size + 1)
        newRouteManager.contains(differentRoute) must be(true)

  "modify route that not found" should "launch not found error" in:
    for
      route          <- validateRoute
      differentRoute <- validateDifferentRoute
    yield emptyManager.modify(route, differentRoute) must be(Errors.NotFound.asLeft[RouteManager])

  "modify route in a route that not exist" should "apply modify" in:
    for
      route      <- validateRoute
      equalRoute <- validateEqualRoute
    yield singleElementManager.modify(route, equalRoute) match
      case Left(error) => fail(s"${error.msg}")
      case Right(newManager) =>
        newManager.size must be(singleElementManager.size)
        newManager.find(equalRoute) match
          case Left(error)      => fail(s"${error.msg}")
          case Right(findRoute) => findRoute.checkAllField(equalRoute) must be(true)

  "modify route in equals of a other route" should "launch already exit error" in:
    for
      route          <- validateRoute
      differentRoute <- validateDifferentRoute
    yield singleElementManager.save(differentRoute) match
      case Left(errors) => fail(s"${errors.msg}")
      case Right(newManager) =>
        newManager.modify(differentRoute, route) must be(Errors.AlreadyExist.asLeft[RouteManager])

  "delete route that non exist" should "launch not exist error" in:
    for
      route <- validateRoute
    yield
      emptyManager.deleteBy(route.id) must be(emptyManager.delete(route))
      emptyManager.deleteBy(route.id) must be(Errors.NotExist.asLeft[RouteManager])

  "delete route that exist" should "have size 0" in:
    for
      route <- validateRoute
    yield
      singleElementManager.deleteBy(route.id) must be(singleElementManager.delete(route))
      singleElementManager.deleteBy(route.id) match
        case Left(error) => fail(s"${error.msg}")
        case Right(newManager) =>
          newManager.contains(route) must be(false)
          newManager.findBy(route.id).isLeft must be(true)
          newManager.size must be(singleElementManager.size - 1)
