package ulisse.applications.ports

import ulisse.applications.station.StationMap
import ulisse.applications.useCases.StationManager
import ulisse.entities.Location
import ulisse.entities.station.Station

/** Defines the `StationPort` object containing inbound operations for managing a `StationMap`.
  */
object StationPort:

  trait Output:
    def show(): Unit

  /** Trait representing inbound operations for interacting with a `StationMap`.
    *
    * @tparam L
    *   The type of location used by the stations.
    */
  trait Input[L <: Location]

//    /** Retrieves the current station map.
//      *
//      * @return
//      *   The `StationMap` containing the stations.
//      */
//    def stationMap: Seq[(String, String, String, String)]
//
//    /** Adds a station to the station map.
//      *
//      * @param station
//      *   The station to be added.
//      * @return
//      *   Either the updated `StationMap` or an `Error` indicating the issue.
//      */
//    def addStation(
//        name: String,
//        x: String,
//        y: String,
//        numberOfTracks: String
//    ): Either[String, List[(String, String, String, String)]]
//
//    /** Removes a station from the station map.
//      *
//      * @param station
//      *   The station to be removed.
//      * @return
//      *   Either the updated `StationMap` or an `Error` indicating the issue.
//      */
//    def removeStation(
//        name: String,
//        x: String,
//        y: String,
//        numberOfTracks: String
//    ): Either[String, List[(String, String, String, String)]]
//
//    /** Finds a station at a specified location.
//      *
//      * @param location
//      *   The location to search for a station.
//      * @return
//      *   An `Option` containing the station at the specified location, if it exists.
//      */
//    def findStationAt(x: String, y: String): Option[List[(String, String, String, String)]]
