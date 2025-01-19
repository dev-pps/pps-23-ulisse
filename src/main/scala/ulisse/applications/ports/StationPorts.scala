package ulisse.applications.ports

import ulisse.applications.station.StationMap
import ulisse.applications.useCases.StationManager
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

object StationPorts:
  /** Trait representing output operations that a `StationManager` uses.
    */
  trait Output

  /** Trait representing input operations for interacting with a `StationManager`.
    *
    * @tparam N
    *   The numeric type representing the coordinates of the station (e.g., `Int`, `Double`).
    *   - An instance of `Numeric` must be available for the `N` type.
    * @tparam C
    *   A type that extends `Coordinate[N]`, which represents the station's location.
    *   - The `C` type must provide a way to compare coordinates and ensure uniqueness.
    */
  trait Input[N: Numeric, C <: Coordinate[N]]:

    /** Retrieves the current station map.
      *
      * @return
      *   The `StationMap` containing the stations.
      */
    def stationMap: StationMap[N, C]

    /** Adds a station to the station map.
      *
      * @param station
      *   The station to be added.
      * @return
      *   Either the updated `StationMap` or an `Error` indicating the issue.
      */
    def addStation(station: Station[N, C]): Either[StationMap.Error, StationMap[N, C]]

    /** Removes a station from the station map.
      *
      * @param station
      *   The station to be removed.
      * @return
      *   Either the updated `StationMap` or an `Error` indicating the issue.
      */
    def removeStation(station: Station[N, C]): Either[StationMap.Error, StationMap[N, C]]

    /** Finds a station at a specified location.
      *
      * @param coordinate
      *   The coordinate to search for a station.
      * @return
      *   An `Option` containing the station at the specified location, if it exists.
      */
    def findStationAt(coordinate: C): Option[Station[N, C]]
