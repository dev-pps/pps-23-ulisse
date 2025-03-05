package ulisse.entities.station

import ulisse.entities.route.Routes.{Route, RouteType}

object Test extends App:
  val stationA = Station("A", Coordinate(0,0), 1)
  val stationB = Station("B", Coordinate(0,0), 1)
  val route1 = Route(stationA, stationB, RouteType.Normal, 1, 100)
  val route2 = Route(Station("A"), Station("B"), RouteType.Normal, 1, 120)
  
  println(route1 == route2)
