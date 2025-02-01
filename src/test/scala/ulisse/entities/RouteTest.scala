package ulisse.entities

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Route.{Path, Station, TypeRoute}

class RouteTest extends AnyFlatSpec with Matchers:
  val riminiStation: Station = ("Rimini", Coordinate.geo(20d, 20d))
  val cesenaStation: Station = ("Cesena", Coordinate.geo(10d, 10d))
  val path: Path             = (riminiStation, cesenaStation)
  val pathLength: Double     = 200.0d
  val railsCount: Int        = 1
  val route: Route           = Route(TypeRoute.Normal, path, pathLength, railsCount)

  "create routes" should "set core parameters: typology, railsCount, path" in:
    route.typology must be(TypeRoute.Normal)
    route.path must be(path)
    route.length must be(pathLength)
    route.railsCount must be(railsCount)

  "check same routes" should "be same typology and path and the others parameters different" in:
    val sameRoute: Route = Route(TypeRoute.Normal, path, 100.0d, 1)
    route must be(sameRoute)

  "check different routes" should "be different typology or path" in:
    val bolognaStation: Station = ("Bologna", Coordinate.geo(30d, 30d))
    val differentPath: Route =
      Route(route.typology, (riminiStation, bolognaStation), pathLength, railsCount)

    route must not be differentPath
    TypeRoute.values.filter(!_.canEqual(route.typology))
      .foreach(route must not be Route(_, path, pathLength, railsCount))

  "set routes typology" should "change routes typology" in:
    val changeRouteTypology = route.setTopology(TypeRoute.AV)
    changeRouteTypology.typology must be(TypeRoute.AV)

  "set routes length" should "change routes length" in:
    val changeRouteLength = route.setLength(pathLength - 50.0d)
    changeRouteLength.length must be(pathLength - 50.0d)
