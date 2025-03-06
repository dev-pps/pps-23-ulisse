package ulisse.entities.station

import ulisse.entities.Coordinate
import ulisse.entities.route.Routes.{Route, RouteType}

object Test extends App:
  val stationA = Station("A", Coordinate(0, 0), 10)
  val stationB = Station("B", Coordinate(1, 1), 10)
  val route1   = Route(stationA, stationB, RouteType.Normal, 1, 100)
  val route2   = Route(stationA, stationB, RouteType.Normal, 4, 120)
  (route1, route2) match
    case (Right(r1), Right(r2)) => println(r1 === r2); println(r1 == r2)
