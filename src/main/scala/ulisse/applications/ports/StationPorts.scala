package ulisse.applications.ports

import cats.data.NonEmptyChain
import ulisse.applications.managers.StationManager
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station

import scala.concurrent.Future

/** Defines the ports related to Stations. */
object StationPorts:
  /** Input operations for interacting with a `StationManager`. */
  trait Input:
    /** The type representing the collection of stations. */
    type SM = StationManager#StationMapType

    /** The type representing the errors that can occur during station management. */
    type E = NonEmptyChain[StationManager.Error]

    /** Retrieves the current `Station` collection. */
    def stationMap: Future[SM]

    /** Adds a `Station` to the station manager or return an error if it fails. */
    def addStation(station: Station): Future[Either[E, SM]]

    /** Removes a `Station` from the station manager or return an error if it fails. */
    def removeStation(station: Station): Future[Either[E, (SM, List[Route])]]

    /** Updates a `Station` from the station manager or return an error if it fails. */
    def updateStation(oldStation: Station, newStation: Station): Future[Either[E, (SM, List[Route])]]

    /** Finds a `Station` at a specified location. */
    def findStationAt(coordinate: Coordinate): Future[Option[Station]]
