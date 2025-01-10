package core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import core.Route
import core.Route.{Path, Station, TypeRoute}
import utils.Points

class RouteTest extends AnyFlatSpec with Matchers:
  val railsCount             = 1
  val riminiStation: Station = ("Cesena", (20d, 20d))
  val cesenaStation: Station = ("Rimini", (10d, 10d))
  val path: Path             = (riminiStation, cesenaStation)
  val route: Route           = Route(TypeRoute.Normal, railsCount, path)

  "create route" should "set core parameters: typology, railsCount, path" in:
    route.typology must be(TypeRoute.Normal)
    route.railsCount must be(railsCount)
    route.path must be(path)

  "compute route length" should "be from path" in:
    route.length must be(Points.computePointsDistance(
      riminiStation._2,
      cesenaStation._2
    ))

  "check same route" should "be same typology and path, but different railsCount" in:
    val sameRoute = Route(TypeRoute.Normal, 1, path)
    route must be(sameRoute)

  "check different route" should "be different typology or path" in:
    val bolognaStation: Station = ("Bologna", (30d, 30d))
    val differentTypology       = Route(TypeRoute.AV, railsCount, path)
    val differentPath =
      Route(TypeRoute.Normal, railsCount, (riminiStation, bolognaStation))

    route must not be differentTypology
    route must not be differentPath
