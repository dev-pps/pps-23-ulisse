package ulisse.dsl

import ulisse.entities.Coordinate
import ulisse.entities.station.Station

/** DSL for creating railway entities. */
object RailwayDsl:

  /** Create a station. */
  object CreateStation:

    /** Create a station with a name. */
    case class StationBuilder(name: String):
      def at(coord: (Int, Int)): StationWithCoord = StationWithCoord(name, Coordinate(coord._1, coord._2))

    /** Create a station with a name and a coordinate. */
    case class StationWithCoord(name: String, coordinate: Coordinate):
      def platform(capacity: Int): Station = Station(name, coordinate, capacity)

    /** Create a station with a name and a coordinate. */
    implicit class StationOps(start: CreateStation.type):
      def ->(name: String): StationBuilder = StationBuilder(name)
