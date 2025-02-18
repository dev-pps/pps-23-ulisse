package ulisse.entities.route

import cats.data.NonEmptyChain
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.entities.route.Routes
import ulisse.entities.route.Routes.{Route, TypeRoute}
import ulisse.entities.station.Station
import ulisse.entities.Coordinate
import ulisse.utils.ValidationUtils.mkStringErrors

object RouteTest:
  type ValidationRoute = Either[NonEmptyChain[Routes.Errors], Route]

  private val departureCoordinate = Coordinate(2, 0)
  private val arrivalCoordinate   = Coordinate(0, 2)
  val railsCount: Int             = 2
  val departure: Station          = Station("Rimini", departureCoordinate, railsCount)
  val arrival: Station            = Station("Cesena", arrivalCoordinate, railsCount)
  val typeRoute: TypeRoute        = TypeRoute.Normal
  val pathLength: Double          = departureCoordinate.distance(arrivalCoordinate)

class RouteTest extends AnyFlatSpec with Matchers:
  import RouteTest.*
  val validateRoute: ValidationRoute = Route(departure, arrival, typeRoute, railsCount, pathLength)

  "create routes" should "set core parameters: typology, railsCount, path" in:
    validateRoute match
      case Left(errors) => fail(s"${errors.mkStringErrors}")
      case Right(route) =>
        route.departure must be(departure)
        route.arrival must be(arrival)
        route.typology must be(typeRoute)
        route.technology must be(typeRoute.technology)
        route.railsCount must be(railsCount)
        route.length must be(pathLength)

  "check equals routes" should "be same typology and path and the others parameters different" in:
    val equalRoute: ValidationRoute = Route(departure, arrival, typeRoute, railsCount - 1, pathLength + 1)
    validateRoute must be(equalRoute)

  "check different routes" should "have different departure or arrival station or typology " in:
    val stationTracks: Int                     = 2
    val newDeparture: Station                  = Station("Firenze", departureCoordinate, stationTracks)
    val newArrival: Station                    = Station("Bologna", arrivalCoordinate, stationTracks)
    val routeWithNewDeparture: ValidationRoute = Route(newDeparture, arrival, typeRoute, railsCount, pathLength)
    val routeWithNewArrival: ValidationRoute   = Route(departure, newArrival, typeRoute, railsCount, pathLength)

    validateRoute must not be routeWithNewDeparture
    validateRoute must not be routeWithNewArrival
    TypeRoute.values.filter(!_.canEqual(typeRoute))
      .foreach(validateRoute must not be Route(departure, arrival, _, railsCount, pathLength))

  "check all fields from equal route" should "be equals" in:
    for
      route <- validateRoute
    yield route.checkAllField(route) must be(true)

  "check all fields from different route" should "be different" in:
    for
      route          <- validateRoute
      differentRoute <- Route(departure, arrival, TypeRoute.AV, railsCount, pathLength + 100)
    yield route.checkAllField(differentRoute) must be(false)

  "route with departure" should "change routes departure" in:
    for
      route <- validateRoute
    yield
      val newDeparture: Station                 = Station("Firenze", departure.coordinate, 2)
      val changeRouteDeparture: ValidationRoute = route.withDeparture(newDeparture)

      changeRouteDeparture match
        case Left(errors) => fail(s"${errors.mkStringErrors}")
        case Right(newRoute) =>
          route.departure must not be newDeparture
          newRoute.departure must be(newDeparture)

  "route with arrival" should "change routes arrival" in:
    for
      route <- validateRoute
    yield
      val newArrival: Station                 = Station("Bologna", arrival.coordinate, 2)
      val changeRouteArrival: ValidationRoute = route.withArrival(newArrival)

      changeRouteArrival match
        case Left(errors) => fail(s"${errors.mkStringErrors}")
        case Right(newRoute) =>
          route.arrival must not be newArrival
          newRoute.arrival must be(newArrival)

  "route with typology" should "change routes typology" in:
    for
      route <- validateRoute
    yield
      val changeRouteTypology = route.withTypology(TypeRoute.AV)

      route.typology must not be TypeRoute.AV
      changeRouteTypology.typology must be(TypeRoute.AV)

  "route with rails count" should "change routes rails count" in:
    for
      route <- validateRoute
    yield
      val newRailsCount: Int                     = railsCount - 1
      val changeRouteRailsCount: ValidationRoute = route.withRailsCount(newRailsCount)

      changeRouteRailsCount match
        case Left(errors) => fail(s"${errors.mkStringErrors}")
        case Right(newRoute) =>
          route.railsCount must not be newRailsCount
          newRoute.railsCount must be(newRailsCount)

  "route with length" should "change routes length" in:
    for
      route <- validateRoute
    yield
      val newLength: Double                  = pathLength + 50.0d
      val changeRouteLength: ValidationRoute = route.withLength(newLength)

      changeRouteLength match
        case Left(errors) => fail(s"${errors.mkStringErrors}")
        case Right(newRoute) =>
          route.length must not be newLength
          newRoute.length must be(newLength)
