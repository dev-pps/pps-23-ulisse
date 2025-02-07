package ulisse.applications.ports

import ulisse.applications.managers.StationManager
import StationManager.CheckedStationManager
import cats.data.NonEmptyChain
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

import scala.concurrent.Future

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
  trait Input[N: Numeric, C <: Coordinate[N], S <: Station[N, C]]:

    type SM = CheckedStationManager[N, C, S]
    type E  = NonEmptyChain[CheckedStationManager.Error]

    /** Retrieves the current station manager.
      *
      * @return
      *   The `StationMap` containing the stations.
      */
    def stationManager: Future[SM]

    /** Adds a station to the station manager.
      *
      * @param station
      *   The station to be added.
      * @return
      *   Either the updated `StationManager` or a `NonEmptyChain` of `Errors` indicating the issues.
      */
    def addStation(station: S): Future[Either[E, SM]]

    /** Removes a station from the station manager.
      *
      * @param station
      *   The station to be removed.
      * @return
      *   Either the updated `StationManager` or a `NonEmptyChain` of `Errors` indicating the issues.
      */
    def removeStation(station: S): Future[Either[E, SM]]

    /** Finds a station at a specified location.
      *
      * @param coordinate
      *   The coordinate to search for a station.
      * @return
      *   An `Option` containing the station at the specified location, if it exists.
      */
    def findStationAt(coordinate: C): Future[Option[S]]
