package ulisse.entities

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.entities.Route.{Path, Station, TypeRoute}

class RouteTest extends AnyFlatSpec with Matchers:
  val riminiStation: Station = ("Cesena", Coordinate.createGeo(20d, 20d))
  val cesenaStation: Station = ("Rimini", Coordinate.createGeo(10d, 10d))
  val path: Path             = (riminiStation, cesenaStation)
  val pathLength: Double     = 200.0d
  val railsCount: Int        = 1
  val route: Route           = Route(TypeRoute.Normal, path, pathLength, railsCount)

  "create route" should "set core parameters: typology, railsCount, path" in:
    route.typology must be(TypeRoute.Normal)
    route.path must be(path)
    route.length must be(pathLength)
    route.railsCount must be(railsCount)

  "check same route" should "be same typology and path, but different railsCount" in:
    val sameRoute: Route = Route(TypeRoute.Normal, path, pathLength, 1)
    route must be(sameRoute)

  "check different route" should "be different typology or path" in:
    val bolognaStation: Station  = ("Bologna", Coordinate.createGeo(30d, 30d))
    val differentTypology: Route = Route(TypeRoute.AV, path, pathLength, railsCount)
    val differentPath: Route =
      Route(TypeRoute.Normal, (riminiStation, bolognaStation), pathLength, railsCount)

    route must not be differentTypology
    route must not be differentPath
