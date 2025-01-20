package ulisse.entities

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.entities.Route.{Path, Station, TypeRoute}

class RouteTest extends AnyFlatSpec with Matchers:
  val riminiStation: Station = ("Cesena", Coordinate(20d, 20d))
  val cesenaStation: Station = ("Rimini", Coordinate(10d, 10d))
  val path: Path             = (riminiStation, cesenaStation)
  val pathLength: Double     = 200.0d
  val railsCount: Int        = 1
  val route: Route           = Route(TypeRoute.Normal, path, pathLength, railsCount)

  "create route" should "set core parameters: typology, railsCount, path" in:
    route.typology must be(TypeRoute.Normal)
    route.path must be(path)
    route.length must be(pathLength)
    route.railsCount must be(railsCount)

  "check same route" should "be same typology and path and the others parameters different" in:
    val sameRoute: Route = Route(TypeRoute.Normal, path, 100.0d, 1)
    route must be(sameRoute)

  "check different route" should "be different typology or path" in:
    val bolognaStation: Station = ("Bologna", Coordinate(30d, 30d))
    val differentPath: Route =
      Route(route.typology, (riminiStation, bolognaStation), pathLength, railsCount)

    route must not be differentPath
    TypeRoute.values.filter(!_.canEqual(route.typology))
      .foreach(route must not be Route(_, path, pathLength, railsCount))

  "set route typology" should "change route typology" in:
    val changeRouteTypology = route.setTopology(TypeRoute.AV)
    changeRouteTypology.typology must be(TypeRoute.AV)

  "set route length" should "change route length" in:
    val changeRouteLength = route.setLength(pathLength - 50.0d)
    changeRouteLength.length must be(pathLength - 50.0d)
