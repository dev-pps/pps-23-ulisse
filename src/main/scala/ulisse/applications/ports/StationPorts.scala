package ulisse.applications.ports

import cats.data.NonEmptyChain
import ulisse.applications.managers.StationManager
import ulisse.entities.Coordinate
import ulisse.entities.station.Station

import scala.concurrent.Future

object StationPorts:
  /** Trait representing input operations for interacting with a `StationManager`.
    *
    * @tparam S
    *   The type of the station that the underlying manager will handle.
    */
  trait Input:

    type SM = StationManager#StationMapType
    type E  = NonEmptyChain[StationManager.Error]

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
    def addStation(station: Station): Future[Either[E, SM]]

    /** Removes a station from the station manager.
      *
      * @param station
      *   The station to be removed.
      * @return
      *   Either the updated `StationMap` or a `NonEmptyChain` of `Errors` indicating the issues.
      */
    def removeStation(station: Station): Future[Either[E, SM]]

    /** Updates a station from the station manager.
      *
      * @param oldStation
      *   The station to update.
      * @param newStation
      *   The new station that replaces the old station.
      * @return
      *   Either the updated `StationMap` or a `NonEmptyChain` of `Errors` indicating the issues.
      */
    def updateStation(oldStation: Station, newStation: Station): Future[Either[E, SM]]

    /** Finds a station at a specified location.
      *
      * @param coordinate
      *   The coordinate to search for a station.
      * @return
      *   An `Option` containing the station at the specified location, if it exists.
      */
    def findStationAt(coordinate: Coordinate): Future[Option[Station]]
