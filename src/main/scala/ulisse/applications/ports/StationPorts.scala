package ulisse.applications.ports

import cats.data.NonEmptyChain
import ulisse.applications.managers.StationManager
import ulisse.entities.Coordinate
import ulisse.entities.station.Station

import scala.concurrent.Future

object StationPorts:
  /** Input operations for interacting with a `StationManager`. */
  trait Input:

    type SM = StationManager#StationMapType
    type E  = NonEmptyChain[StationManager.Error]

    /** Retrieves the current `Station` collection. */
    def stationMap: Future[SM]

    /** Adds a `Station` to the station manager or return an error if it fails. */
    def addStation(station: Station): Future[Either[E, SM]]

    /** Removes a `Station` from the station manager or return an error if it fails. */
    def removeStation(station: Station): Future[Either[E, SM]]

    /** Updates a `Station` from the station manager or return an error if it fails. */
    def updateStation(oldStation: Station, newStation: Station): Future[Either[E, SM]]

    /** Finds a `Station` at a specified location. */
    def findStationAt(coordinate: Coordinate): Future[Option[Station]]
