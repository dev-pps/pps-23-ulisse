package ulisse.entities

import cats.data.NonEmptyChain
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.{Route, TypeRoute}
import ulisse.entities.station.Station
import ulisse.utils.ValidationUtils.mkStringErrors

object RouteTest extends AnyFlatSpec with Matchers:
  opaque type ValueType   = Double
  opaque type StationTest = Station[ValueType, Coordinate[ValueType]]
  opaque type RouteTest   = Either[NonEmptyChain[Routes.Errors], Route[ValueType, Coordinate[ValueType]]]

  private val railsCount: Int        = 2
  private val departure: StationTest = Station("Rimini", Coordinate.createValidRandomGeo(), railsCount)
  private val arrival: StationTest   = Station("Cesena", Coordinate.createValidRandomGeo(), railsCount)
  private val typeRoute: TypeRoute   = TypeRoute.Normal
  private val pathLength: Double     = departure.coordinate.distance(arrival.coordinate)

  private val validateRoute: RouteTest = Route(departure, arrival, typeRoute, railsCount, pathLength)

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
    val equalRoute: RouteTest = Route(departure, arrival, typeRoute, railsCount - 1, pathLength + 1)
    validateRoute must be(equalRoute)

  "check different routes" should "have different departure or arrival station or typology " in:
    val stationTracks: Int               = 2
    val newDeparture: StationTest        = Station("Firenze", Coordinate.createValidRandomGeo(), stationTracks)
    val newArrival: StationTest          = Station("Bologna", Coordinate.createValidRandomGeo(), stationTracks)
    val routeWithNewDeparture: RouteTest = Route(newDeparture, arrival, typeRoute, railsCount, pathLength)
    val routeWithNewArrival: RouteTest   = Route(departure, newArrival, typeRoute, railsCount, pathLength)

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
      val newDeparture: StationTest       = Station("Firenze", departure.coordinate, 2)
      val changeRouteDeparture: RouteTest = route.withDeparture(newDeparture)

      changeRouteDeparture match
        case Left(errors) => fail(s"${errors.mkStringErrors}")
        case Right(newRoute) =>
          route.departure must not be newDeparture
          newRoute.departure must be(newDeparture)

  "route with arrival" should "change routes arrival" in:
    for
      route <- validateRoute
    yield
      val newArrival: StationTest       = Station("Bologna", arrival.coordinate, 2)
      val changeRouteArrival: RouteTest = route.withArrival(newArrival)

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
      val newRailsCount: Int               = railsCount - 1
      val changeRouteRailsCount: RouteTest = route.withRailsCount(newRailsCount)

      changeRouteRailsCount match
        case Left(errors) => fail(s"${errors.mkStringErrors}")
        case Right(newRoute) =>
          route.railsCount must not be newRailsCount
          newRoute.railsCount must be(newRailsCount)

  "route with length" should "change routes length" in:
    for
      route <- validateRoute
    yield
      val newLength: Double            = pathLength + 50.0d
      val changeRouteLength: RouteTest = route.withLength(newLength)

      changeRouteLength match
        case Left(errors) => fail(s"${errors.mkStringErrors}")
        case Right(newRoute) =>
          route.length must not be newLength
          newRoute.length must be(newLength)
