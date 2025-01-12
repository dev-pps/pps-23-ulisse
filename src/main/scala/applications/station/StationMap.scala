//package applications.station
//
//import cats.implicits.catsSyntaxEq
//import entities.Location
//import entities.station.Station
//
//trait StationMap[T[_], L <: Location]:
//  val stations: T[Station[L]]
//
///** Represents a collection of stations for the application.
//  *
//  * @constructor
//  *   create a new StationMap given a list of Station.
//  * @param stations
//  *   the stations in the map
//  * @throws IllegalArgumentException
//  *   If station names or locations are not unique.
//  */
//object StationMap:
//  def apply[L <: Location](stations: List[Station[L]]): StationMap[List, L] =
//    StationMapImpl(stations)
//  private final case class StationMapImpl[L <: Location](
//      stations: List[Station[L]]
//  ) extends StationMap[List, L]:
//    require(
//      stations.map(_.name).distinct.size === stations.size,
//      "station names must be unique"
//    )
//
//    require(
//      stations.map(_.location).distinct.size === stations.size,
//      "station locations must be unique"
//    )
