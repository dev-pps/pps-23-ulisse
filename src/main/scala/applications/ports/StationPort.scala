package applications.ports

import applications.station.StationMap
import applications.usecase.StationManager
import entities.Location
import entities.station.Station

/** Defines the `StationPort` object containing inbound operations for managing
  * a `StationMap`.
  */
object StationPort:

  trait Outbound:
    def show(): Unit

  /** Trait representing inbound operations for interacting with a `StationMap`.
    *
    * @tparam L
    *   The type of location used by the stations.
    */
  trait Inbound[L <: Location]:

    /** Retrieves the current station map.
      *
      * @return
      *   The `StationMap` containing the stations.
      */
    def stationMap: StationMap[L]

    /** Adds a station to the station map.
      *
      * @param station
      *   The station to be added.
      * @return
      *   Either the updated `StationMap` or an `Error` indicating the issue.
      */
    def addStation(station: Station[L]): Either[StationMap.Error, StationMap[L]]

    /** Removes a station from the station map.
      *
      * @param station
      *   The station to be removed.
      * @return
      *   Either the updated `StationMap` or an `Error` indicating the issue.
      */
    def removeStation(station: Station[L])
        : Either[StationMap.Error, StationMap[L]]

    /** Finds a station at a specified location.
      *
      * @param location
      *   The location to search for a station.
      * @return
      *   An `Option` containing the station at the specified location, if it
      *   exists.
      */
    def findStationAt(location: L): Option[Station[L]]
