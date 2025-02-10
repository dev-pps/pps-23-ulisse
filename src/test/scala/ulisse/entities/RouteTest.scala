package ulisse.entities

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.Route
import ulisse.entities.Routes.Route.TypeRoute
import ulisse.entities.station.Station

class RouteTest extends AnyFlatSpec with Matchers:
  opaque type ValueType   = Double
  opaque type StationTest = Station[ValueType, Coordinate[ValueType]]
  opaque type RouteTest   = Route[ValueType, Coordinate[ValueType]]

  val coordinate = Station

  val departureStation: StationTest = Station("Rimini", Coordinate.createValidRandomGeo(), 2)
  val arrivalStation: StationTest   = Station("Cesena", Coordinate.createValidRandomGeo(), 2)
  val typeRoute: TypeRoute          = TypeRoute.Normal
  val railsCount: Int               = 1
  val pathLength: Double            = 200.0d

  val route: RouteTest = Route(departureStation, arrivalStation, typeRoute, railsCount, pathLength)

  "create routes" should "set core parameters: typology, railsCount, path" in:
    route.departure must be(departureStation)
    route.arrival must be(arrivalStation)
    route.typology must be(typeRoute)
    route.technology must be(typeRoute.technology)
    route.railsCount must be(railsCount)
    route.length must be(pathLength)

  "check same routes" should "be same typology and path and the others parameters different" in:
    val sameRoute: RouteTest = Route(departureStation, arrivalStation, typeRoute, railsCount + 1, pathLength + 1)
    route must be(sameRoute)

  "check different routes" should "have different departure or arrival station or typology " in:
    val newDeparture: StationTest        = Station("Firenze", Coordinate.createValidRandomGeo(), 2)
    val newArrival: StationTest          = Station("Bologna", Coordinate.createValidRandomGeo(), 2)
    val routeWithNewDeparture: RouteTest = Route(newDeparture, arrivalStation, route.typology, railsCount, pathLength)
    val routeWithNewArrival: RouteTest   = Route(departureStation, newArrival, route.typology, railsCount, pathLength)

    route must not be routeWithNewDeparture
    route must not be routeWithNewArrival
    TypeRoute.values.filter(!_.canEqual(route.typology))
      .foreach(route must not be Route(departureStation, arrivalStation, _, railsCount, pathLength))

  "route with departure" should "change routes departure" in:
    val newDeparture: StationTest       = Station("Firenze", Coordinate.createValidRandomGeo(), 2)
    val changeRouteDeparture: RouteTest = route.withDeparture(newDeparture)
    route.departure must not be newDeparture
    changeRouteDeparture.departure must be(newDeparture)

  "route with arrival" should "change routes arrival" in:
    val newArrival: StationTest       = Station("Bologna", Coordinate.createValidRandomGeo(), 2)
    val changeRouteArrival: RouteTest = route.withArrival(newArrival)
    route.arrival must not be newArrival
    changeRouteArrival.arrival must be(newArrival)

  "route with typology" should "change routes typology" in:
    val changeRouteTypology = route.withTypology(TypeRoute.AV)
    route.typology must not be TypeRoute.AV
    changeRouteTypology.typology must be(TypeRoute.AV)

  "route with rails count" should "change routes rails count" in:
    val changeRouteRailsCount = route.withRailsCount(railsCount + 1)
    route.railsCount must not be (railsCount + 1)
    changeRouteRailsCount.railsCount must be(railsCount + 1)

  "route with length" should "change routes length" in:
    val changeRouteLength = route.withLength(pathLength - 50.0d)
    route.length must not be (pathLength - 50.0d)
    changeRouteLength.length must be(pathLength - 50.0d)
