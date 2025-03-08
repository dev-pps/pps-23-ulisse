package ulisse.applications.managers

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.Coordinate
import ulisse.entities.station.Station
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.validateUniqueItems

/** Defines a manager for stations.
  *
  * A `StationManager` is a collection of `Station` instances.
  */
trait StationManager:

  /** The type representing the collection of stations. */
  type StationMapType <: Seq[Station]

  /** The collection of stations in the manager. */
  val stations: StationMapType

  /** Adds a station to the manager. If the station is not accepted by the manager an error is returned */
  def addStation(station: Station): Either[NonEmptyChain[StationManager.Error], StationManager]

  /** Updates a station in the manager. If the old station is not recognized by the manager or the new one is not accepted an error is returned */
  def updateStation(
      oldStation: Station,
      newStation: Station
  ): Either[NonEmptyChain[StationManager.Error], StationManager] =
    removeStation(oldStation).flatMap(_.addStation(newStation))

  /** Remove a station from the manager. If the station is not recognized by the manager an error is returned */
  def removeStation(station: Station): Either[NonEmptyChain[StationManager.Error], StationManager]

  /** Finds a station at the given location. */
  def findStationAt(coordinate: Coordinate): Option[Station]

/** Factory for [[StationManager]] instances. */
object StationManager:
  /** Creates a `StationManager` instance, which is a `StationManager` with validation for unique `Stations`. */
  def apply(): StationManager = StationManagerImpl(List.empty)

  private final case class StationManagerImpl(stations: List[Station]) extends StationManager:
    type StationMapType = List[Station]

    override def addStation(station: Station): Either[NonEmptyChain[StationManager.Error], StationManager] =
      val updatedStations = station :: stations
      (
        validateUniqueItems(
          updatedStations.map(_.name),
          StationManager.Error.DuplicateStationName
        ).toValidatedNec,
        validateUniqueItems(
          updatedStations.map(_.coordinate),
          StationManager.Error.DuplicateStationLocation
        ).toValidatedNec
      )
        .mapN((_, _) => StationManagerImpl(updatedStations)).toEither

    override def removeStation(station: Station): Either[NonEmptyChain[StationManager.Error], StationManager] =
      if stations.contains(station) then Right(StationManagerImpl(stations.filterNot(_ == station)))
      else Left(NonEmptyChain(StationManager.Error.StationNotFound))

    override def findStationAt(coordinate: Coordinate): Option[Station] =
      stations.find(_.coordinate == coordinate)

  /** Represents errors that can occur during [[StationManager]] usage. */
  enum Error extends BaseError:
    case DuplicateStationName, DuplicateStationLocation, StationNotFound
