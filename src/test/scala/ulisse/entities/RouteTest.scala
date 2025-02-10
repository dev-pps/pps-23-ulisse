package ulisse.entities

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.Route
import ulisse.entities.Routes.Route.TypeRoute
import ulisse.entities.station.Station

class RouteTest extends AnyFlatSpec with Matchers:
  opaque type ValueType = Double

  val departureStation: Station[ValueType, Coordinate[ValueType]] = Station("Rimini", Coordinate.geo(20d, 20d), 2)
  val arrivalStation: Station[ValueType, Coordinate[ValueType]]   = Station("Cesena", Coordinate.geo(10d, 10d), 2)
  val typeRoute: TypeRoute                                        = TypeRoute.Normal
  val railsCount: Int                                             = 1
  val pathLength: Double                                          = 200.0d

  val route: Route[ValueType, Coordinate[ValueType]] =
    Route(departureStation, arrivalStation, typeRoute, railsCount, pathLength)

  "create routes" should "set core parameters: typology, railsCount, path" in:
    route.departure must be(departureStation)
    route.arrival must be(arrivalStation)
    route.typology must be(typeRoute)
    route.railsCount must be(railsCount)
    route.length must be(pathLength)

  "check same routes" should "be same typology and path and the others parameters different" in:
    val sameRoute = Route(departureStation, arrivalStation, typeRoute, railsCount + 1, pathLength + 1)
    route must be(sameRoute)

  "check different routes" should "be different typology or path" in:
    val newArrival: Station[Double, Coordinate[Double]] = Station("Bologna", Coordinate.geo(30d, 30d), 2)
    val routeWithNewArrival = Route(departureStation, newArrival, route.typology, railsCount, pathLength)

    route must not be routeWithNewArrival
    TypeRoute.values.filter(!_.canEqual(route.typology))
      .foreach(route must not be Route(departureStation, arrivalStation, _, railsCount, pathLength))

  "set routes typology" should "change routes typology" in:
    val changeRouteTypology = route.withTechnology(TypeRoute.AV)
    changeRouteTypology.typology must be(TypeRoute.AV)

  "set routes length" should "change routes length" in:
    val changeRouteLength = route.withLength(pathLength - 50.0d)
    changeRouteLength.length must be(pathLength - 50.0d)
