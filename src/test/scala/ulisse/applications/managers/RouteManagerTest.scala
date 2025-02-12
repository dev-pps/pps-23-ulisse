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

  val railsCount: Int        = 2
  val departure: StationTest = Station("Rimini", Coordinate.createValidRandomGeo(), railsCount)
  val arrival: StationTest   = Station("Cesena", Coordinate.createValidRandomGeo(), railsCount)
  val typeRoute: TypeRoute   = TypeRoute.Normal
  val pathLength: Double     = departure.coordinate.distance(arrival.coordinate)

  val validateRoute: RouteTest          = Route(departure, arrival, typeRoute, railsCount, pathLength)
  val validateEqualRoute: RouteTest     = Route(departure, arrival, typeRoute, railsCount, pathLength + 100d)
  val validateDifferentRoute: RouteTest = Route(departure, arrival, TypeRoute.AV, railsCount, pathLength)

  val emptyManager: RouteManagerTest = RouteManager.empty()
  val singleElementManager: RouteManagerTest =
    validateRoute.map(route => RouteManager.createOf(List(route))).getOrElse(emptyManager)

  "create empty routeManager" should "have size 0" in:
    val zeroSize = 0
    emptyManager.size must be(zeroSize)

  "check route to test manager" should "be empty" in:
    validateRoute match
      case Left(errors) => fail(s"${errors.mkStringErrors}")
      case Right(route) =>
        validateEqualRoute match
          case Left(errors) => fail(s"${errors.mkStringErrors}")
          case Right(equalRoute) =>
            validateDifferentRoute match
              case Left(errors) => fail(s"${errors.mkStringErrors}")
              case Right(differentRoute) =>
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
      singleElementManager.find(route.id) must be(singleElementManager.findFrom(route))
      singleElementManager.find(route.id) match
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
    yield emptyManager.modify(route, differentRoute) must be(Errors.NotFound.asLeft[RouteManagerTest])

  "modify route in a route that not exist" should "apply modify" in:
    for
      route      <- validateRoute
      equalRoute <- validateEqualRoute
    yield singleElementManager.modify(route, equalRoute) match
      case Left(error) => fail(s"${error.msg}")
      case Right(newManager) =>
        newManager.size must be(singleElementManager.size)
        newManager.findFrom(equalRoute) match
          case Left(error)      => fail(s"${error.msg}")
          case Right(findRoute) => findRoute.checkAllField(equalRoute) must be(true)

  "modify route in equals of a other route" should "launch already exit error" in:
    for
      route          <- validateRoute
      differentRoute <- validateDifferentRoute
    yield singleElementManager.save(differentRoute) match
      case Left(errors) => fail(s"${errors.msg}")
      case Right(newManager) =>
        newManager.modify(differentRoute, route) must be(Errors.AlreadyExist.asLeft[RouteManagerTest])

  "delete route that non exist" should "launch not exist error" in:
    for
      route <- validateRoute
    yield
      emptyManager.delete(route.id) must be(emptyManager.deleteForm(route))
      emptyManager.delete(route.id) must be(Errors.NotExist.asLeft[RouteManagerTest])

  "delete route that exist" should "have size 0" in:
    for
      route <- validateRoute
    yield
      singleElementManager.delete(route.id) must be(singleElementManager.deleteForm(route))
      singleElementManager.delete(route.id) match
        case Left(error) => fail(s"${error.msg}")
        case Right(newManager) =>
          newManager.contains(route) must be(false)
          newManager.find(route.id).isLeft must be(true)
          newManager.size must be(singleElementManager.size - 1)
