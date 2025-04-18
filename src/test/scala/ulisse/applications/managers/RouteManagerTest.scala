package ulisse.applications.managers

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.applications.managers.RouteManagers.{Errors, RouteManager}
import ulisse.entities.Coordinate
import ulisse.entities.route.RouteTest.*
import ulisse.entities.route.Routes.*
import ulisse.entities.station.Station

object RouteManagerTest:
  val validateRoute: ValidationRoute          = Route(departure, arrival, routeType, railsCount, pathLength)
  val validateEqualRoute: ValidationRoute     = Route(departure, arrival, routeType, railsCount, pathLength + 100d)
  val validateDifferentRoute: ValidationRoute = Route(departure, arrival, RouteType.AV, railsCount, pathLength)

  val emptyManager: RouteManager = RouteManager.empty()
  val singleElementManager: RouteManager =
    validateRoute map (route => RouteManager.createOf(List(route))) getOrElse emptyManager

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
    for
      route      <- validateRoute
      newManager <- emptyManager save route
    yield
      newManager.size mustBe (emptyManager.size + 1)
      newManager contains route mustBe true

  "find route from manager that" should "contains" in:
    for route <- validateRoute
    yield
      singleElementManager findBy route.id mustBe (singleElementManager find route)
      singleElementManager find route mustBe Right(route)

  "find route from manager that" should "not contains" in:
    for route <- validateRoute
    yield
      emptyManager findBy route.id mustBe (emptyManager find route)
      emptyManager find route mustBe Left(Errors.NotFound)

  "save equal route" should "launch already exist error" in:
    for equalRoute <- validateEqualRoute
    yield singleElementManager save equalRoute mustBe Left(Errors.AlreadyExist)

  "save other different routes" should "have two element" in:
    for
      differentRoute <- validateDifferentRoute
      newManager     <- singleElementManager save differentRoute
    yield
      newManager.size mustBe (singleElementManager.size + 1)
      newManager contains differentRoute mustBe true

  "modify route that not found" should "launch not found error" in:
    for
      route          <- validateRoute
      differentRoute <- validateDifferentRoute
    yield emptyManager modify (route, differentRoute) mustBe Left(Errors.NotFound)

  "modify route in a route that not exist" should "apply modify" in:
    for
      route         <- validateRoute
      equalRoute    <- validateEqualRoute
      updateManager <- singleElementManager modify (route, equalRoute)
      findRoute     <- updateManager find equalRoute
    yield
      updateManager.size mustBe singleElementManager.size
      findRoute checkAllField equalRoute mustBe true

  "modify route in equals of a other route" should "launch already exit error" in:
    for
      route          <- validateRoute
      differentRoute <- validateDifferentRoute
      updateManager  <- singleElementManager save differentRoute
    yield updateManager modify (route, differentRoute) mustBe Left(Errors.AlreadyExist)

  "modify automatic departure station in a route" should "be contains in routeManager" in:
    val newDeparture = Station("New Departure", departure.coordinate, 3)
    val newManager   = singleElementManager modifyAutomaticByDeparture (departure, newDeparture)
    for
      route    <- validateRoute
      newRoute <- newManager find route
    yield
      newManager.size mustBe singleElementManager.size
      route changeAutomaticDeparture newDeparture mustBe newRoute

  "modify automatic arrival station in a route" should "be contains in routeManager" in:
    val newArrival = Station("New Arrival", arrival.coordinate, 3)
    val newManager = singleElementManager modifyAutomaticByArrival (arrival, newArrival)
    for
      route    <- validateRoute
      newRoute <- newManager find route
    yield
      newManager.size mustBe singleElementManager.size
      route changeAutomaticArrival newArrival mustBe newRoute

  "modify automatic station in a route" should "be contains in routeManager" in:
    val newDeparture              = Station("New Departure", departure.coordinate, 3)
    val newArrival                = Station("New Arrival", arrival.coordinate, 3)
    val newManagerChangeDeparture = singleElementManager modifyAutomaticByStation (departure, newDeparture)
    val newManagerChangeArrival   = singleElementManager modifyAutomaticByStation (arrival, newArrival)
    for
      route             <- validateRoute
      newDepartureRoute <- newManagerChangeDeparture find route
      newArrivalRoute   <- newManagerChangeArrival find route
    yield
      newManagerChangeDeparture.size mustBe singleElementManager.size
      newManagerChangeArrival.size mustBe singleElementManager.size

      route changeAutomaticDeparture newDeparture mustBe newDepartureRoute
      route changeAutomaticArrival newArrival mustBe newArrivalRoute

  "delete route that non exist" should "launch not exist error" in:
    for route <- validateRoute
    yield
      emptyManager.deleteBy(route.id) mustBe emptyManager.delete(route)
      emptyManager.delete(route) mustBe Left(Errors.NotExist)

  "delete route that exist" should "have size 0" in:
    for route <- validateRoute
    yield
      singleElementManager.deleteBy(route.id) mustBe singleElementManager.delete(route)
      singleElementManager.delete(route) mustBe Right(emptyManager)

  "find route from departure station" should "be contains in routeManager" in:
    singleElementManager findByDeparture departure mustBe Right(singleElementManager.routes)

  "find route from departure station that not exist" should "be empty" in:
    singleElementManager findByDeparture arrival mustBe Left(Errors.NotFound)

  "find route from arrival station" should "be contains in routeManager" in:
    singleElementManager findByArrival arrival mustBe Right(singleElementManager.routes)

  "find route from arrival station that not exist" should "be empty" in:
    singleElementManager findByArrival departure mustBe Left(Errors.NotFound)

  "find route from path" should "be contains in routeManager" in:
    singleElementManager findByPath (arrival, arrival) mustBe Left(Errors.NotFound)
    singleElementManager findByPath (departure, departure) mustBe Left(Errors.NotFound)
    singleElementManager findByPath (departure, arrival) mustBe Right(singleElementManager.routes)
    singleElementManager findByPath (arrival, departure) mustBe Right(singleElementManager.routes)

  "delete route from departure station" should "be empty" in:
    singleElementManager deleteByDeparture departure mustBe Right(emptyManager)

  "delete route form departure station that not exist" should "be empty" in:
    singleElementManager deleteByDeparture arrival mustBe Left(Errors.NotFound)

  "delete route from arrival station" should "be empty" in:
    singleElementManager deleteByArrival arrival mustBe Right(emptyManager)

  "delete route form arrival station that not exist" should "be empty" in:
    singleElementManager deleteByArrival departure mustBe Left(Errors.NotFound)

  "delete route from station" should "be empty" in:
    singleElementManager deleteByStation departure mustBe Right(emptyManager)
    singleElementManager deleteByStation arrival mustBe Right(emptyManager)

  "delete route form station that not exist" should "be empty" in:
    val station = Station("Not Exist", Coordinate(0, 0), 3)
    station.coordinate must not be departure.coordinate
    singleElementManager deleteByStation station mustBe Left(Errors.NotExist)
