package core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scala.core.Route
import scala.core.Route.{ Path, TypeRoute }


class RouteTest extends AnyFlatSpec with Matchers:

  "create route" should "set core parameters: typology, railsCount, path" in:
    val railsCount = 1
    val path: Path = ("Rimini", "Cesena")
    val route: Route = Route(TypeRoute.Normal, railsCount, path)
    route.typology must be (TypeRoute.Normal)
    route.railsCount must be (railsCount)
    route.path must be (path)





    
