package model.station

import cats.implicits.catsSyntaxEq

/** Represents a collection of stations for the application.
  *
  * @constructor
  *   create a new StationMap given a list of Station.
  * @param stations
  *   the stations in the map
  * @throws IllegalArgumentException
  *   If station names or locations are not unique.
  */
final case class StationMap(stations: List[Station]):
  require(
    stations.map(_.name).distinct.size === stations.size,
    "station names must be unique"
  )

  require(
    stations.map(_.location).distinct.size === stations.size,
    "station locations must be unique"
  )

  export stations.find
