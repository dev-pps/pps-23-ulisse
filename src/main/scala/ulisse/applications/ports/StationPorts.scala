package ulisse.applications.ports

import ulisse.applications.managers.{CheckedStationManager, StationManager}
import StationManager.CheckedStationManager
import cats.data.NonEmptyChain
import ulisse.applications.AppState
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
  trait Input[S <: Station[?]]:

    type SM = CheckedStationManager[S]#StationMapType
    type E  = NonEmptyChain[CheckedStationManager[S]#E]

    /** Retrieves the current station map.
      *
      * @return
      *   The `StationMap` containing the stations.
      */
    def stationMap: Future[SM]

    /** Adds a station to the station manager.
      *
      * @param station
      *   The station to be added.
      * @return
      *   Either the updated `StationMap` or a `NonEmptyChain` of `Errors` indicating the issues.
      */
    def addStation(station: S): Future[Either[E, SM]]

    /** Removes a station from the station manager.
      *
      * @param station
      *   The station to be removed.
      * @return
      *   Either the updated `StationMap` or a `NonEmptyChain` of `Errors` indicating the issues.
      */
    def removeStation(station: Station[?]): Future[Either[E, SM]]

    /** Finds a station at a specified location.
      *
      * @param coordinate
      *   The coordinate to search for a station.
      * @return
      *   An `Option` containing the station at the specified location, if it exists.
      */
    def findStationAt(coordinate: Coordinate[?]): Future[Option[S]]
