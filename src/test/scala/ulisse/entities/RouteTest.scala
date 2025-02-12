package ulisse.entities

import cats.data.NonEmptyChain
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.Route
import ulisse.entities.Routes.TypeRoute
import ulisse.entities.station.Station
import ulisse.utils.ValidationUtils.mkStringErrors

class RouteTest extends AnyFlatSpec with Matchers:
  opaque type ValueType   = Double
  opaque type StationTest = Station[ValueType, Coordinate[ValueType]]
  opaque type RouteTest   = Either[NonEmptyChain[Routes.Errors], Route[ValueType, Coordinate[ValueType]]]

  val departure: StationTest = Station("Rimini", Coordinate.createValidRandomGeo(), 2)
  val arrival: StationTest   = Station("Cesena", Coordinate.createValidRandomGeo(), 2)
  val typeRoute: TypeRoute   = TypeRoute.Normal
  val railsCount: Int        = 1
  val pathLength: Double     = departure.coordinate.distance(arrival.coordinate)

  val creationRoute: RouteTest = Route(departure, arrival, typeRoute, railsCount, pathLength)

  "create routes" should "set core parameters: typology, railsCount, path" in:
    creationRoute match
      case Left(errors) => fail(s"${errors.mkStringErrors}")
      case Right(route) =>
        route.departure must be(departure)
        route.arrival must be(arrival)
        route.typology must be(typeRoute)
        route.technology must be(typeRoute.technology)
        route.railsCount must be(railsCount)
        route.length must be(pathLength)

  "check same routes" should "be same typology and path and the others parameters different" in:
    val sameRoute: RouteTest = Route(departure, arrival, typeRoute, railsCount + 1, pathLength + 1)
    creationRoute must be(sameRoute)

  "check different routes" should "have different departure or arrival station or typology " in:
    val newDeparture: StationTest        = Station("Firenze", Coordinate.createValidRandomGeo(), 2)
    val newArrival: StationTest          = Station("Bologna", Coordinate.createValidRandomGeo(), 2)
    val routeWithNewDeparture: RouteTest = Route(newDeparture, arrival, typeRoute, railsCount, pathLength)
    val routeWithNewArrival: RouteTest   = Route(departure, newArrival, typeRoute, railsCount, pathLength)

    creationRoute must not be routeWithNewDeparture
    creationRoute must not be routeWithNewArrival
    TypeRoute.values.filter(!_.canEqual(typeRoute))
      .foreach(creationRoute must not be Route(departure, arrival, _, railsCount, pathLength))

  "route with departure" should "change routes departure" in:
    val newDeparture: StationTest = Station("Firenze", departure.coordinate, 2)
    creationRoute.foreach(route =>
      val changeRouteDeparture: RouteTest = route.withDeparture(newDeparture)
      changeRouteDeparture match
        case Left(errors) => fail(s"${errors.mkStringErrors}")
        case Right(newRoute) =>
          route.departure must not be newDeparture
          newRoute.departure must be(newDeparture)
    )

  "route with arrival" should "change routes arrival" in:
    val newArrival: StationTest = Station("Bologna", arrival.coordinate, 2)
    creationRoute.foreach(route =>
      val changeRouteArrival: RouteTest = route.withArrival(newArrival)
      changeRouteArrival match
        case Left(errors) => fail(s"${errors.mkStringErrors}")
        case Right(newRoute) =>
          route.arrival must not be newArrival
          newRoute.arrival must be(newArrival)
    )

  "route with typology" should "change routes typology" in:
    creationRoute.foreach(route =>
      val changeRouteTypology = route.withTypology(TypeRoute.AV)
      route.typology must not be TypeRoute.AV
      changeRouteTypology.typology must be(TypeRoute.AV)
    )

  "route with rails count" should "change routes rails count" in:
    creationRoute.foreach(route =>
      val changeRouteRailsCount: RouteTest = route.withRailsCount(railsCount + 1)
      changeRouteRailsCount match
        case Left(errors) => fail(s"${errors.mkStringErrors}")
        case Right(newRoute) =>
          route.railsCount must not be (railsCount + 1)
          newRoute.railsCount must be(railsCount + 1)
    )

  "route with length" should "change routes length" in:
    creationRoute.foreach(route =>
      val changeRouteLength: RouteTest = route.withLength(pathLength + 50.0d)
      changeRouteLength match
        case Left(errors) => fail(s"${errors.mkStringErrors}")
        case Right(newRoute) =>
          route.length must not be (pathLength + 50.0d)
          newRoute.length must be(pathLength + 50.0d)
    )
