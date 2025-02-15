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

  /** Transforms the stations using a function `f`.
    *
    * @tparam B
    *   The type of the result.
    * @param f
    *   The function to apply to each station.
    * @return
    *   A list of results obtained by applying the function to each station.
    */
  def map[B](f: Station => B): List[B]

  /** Compares two `StationMap` instances for equality.
    *
    * @param other
    *   The other object to compare with.
    * @return
    *   `true` if both station maps have the same stations, `false` otherwise.
    */
  override def equals(other: Any): Boolean = other match
    case that: StationManager => that.stations == stations
    case _                    => false

  /** Adds a station to the manager.
    *
    * @param station
    *   The station to add.
    * @return
    *   R type
    */
  def addStation(station: Station): Either[NonEmptyChain[StationManager.Error], StationManager]

  /** Removes a station from the manager.
    *
    * @param station
    *   The station to remove.
    * @return
    *   R type
    */
  def removeStation(station: Station): Either[NonEmptyChain[StationManager.Error], StationManager]

  /** Finds a station at the given location.
    *
    * @param coordinate
    *   The location to search for.
    * @return
    *   An `Option` containing the station at the given location, if found.
    */
  def findStationAt(coordinate: Coordinate): Option[Station]

/** Factory for [[StationManager]] instances. */
object StationManager:
  /** Creates a `StationManager` instance, which is a `StationManager` with validation for unique names and
    * locations.
    *
    * @return
    *   An empty `CheckedStationManager` instance.
    */
  def apply(): StationManager = StationManagerImpl(List.empty)

  private final case class StationManagerImpl(stations: List[Station]) extends StationManager:
    type StationMapType = List[Station]

    def addStation(station: Station): Either[NonEmptyChain[StationManager.Error], StationManager] =
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

    def removeStation(station: Station): Either[NonEmptyChain[StationManager.Error], StationManager] =
      if stations.contains(station) then
        Right(StationManagerImpl(stations.filterNot(_ == station)))
      else
        Left(NonEmptyChain(StationManager.Error.StationNotFound))

    def findStationAt(coordinate: Coordinate): Option[Station] =
      stations.find(_.coordinate == coordinate)

    export stations.map

  /** Represents errors that can occur during [[StationManager]] usage. */
  enum Error extends BaseError:
    case DuplicateStationName, DuplicateStationLocation, StationNotFound
