package ulisse.applications.managers

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.applications.managers.StationManager.CheckedStationManager
import ulisse.entities.Coordinate
import ulisse.entities.station.Station
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.validateUniqueItems

/** Defines a manager for stations.
  *
  * A `StationManager` is a collection of `Station` instances.
  *
  * @tparam S
  *   The station type that the manager will handle.
  */
trait StationManager:

  /** The type representing the collection of stations. */
  type StationMapType <: Seq[Station]

  /** The return type of the methods for adding or removing stations. */
  type R

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
  def addStation(station: Station): R

  /** Removes a station from the manager.
    *
    * @param station
    *   The station to remove.
    * @return
    *   R type
    */
  def removeStation(station: Station): R

  /** Finds a station at the given location.
    *
    * @param coordinate
    *   The location to search for.
    * @return
    *   An `Option` containing the station at the given location, if found.
    */
  def findStationAt(coordinate: Coordinate): Option[Station]

/** Defines a manager for stations.
  *
  * A `UncheckedStationManager` is a `StationManager` that doesn't make checks when stations are added or removed.
  *
  * @tparam S
  *   The station type that the manager will handle.
  */
trait UncheckedStationManager extends StationManager:
  type R = UncheckedStationManager

/** Defines a manager for stations.
  *
  * A `CheckedStationManager` is a `StationManager` that make checks when stations are added or removed.
  *
  * @tparam S
  *   The station type that the manager will handle.
  */
trait CheckedStationManager extends StationManager:
  type R = Either[NonEmptyChain[E], CheckedStationManager]
  type E = CheckedStationManager.Error

/** Factory for [[StationManager]] instances. */
object StationManager:
  /** Creates a `StationManager` instance.
    *
    * @tparam S
    *   The station type that the manager will handle.
    * @return
    *   A `StationManager` instance.
    */
  def apply(stations: Station*): UncheckedStationManager = UncheckedStationManagerImpl(stations.toList)

  /** Creates a `CheckedStationManager` instance, which is a `StationManager` with validation for unique names and
    * locations.
    *
    * @tparam S
    *   The station type that the manager will handle.
    * @return
    *   An empty `CheckedStationManager` instance.
    */
  def createCheckedStationManager(): CheckedStationManager =
    CheckedStationManagerImpl(List.empty)

  private final case class CheckedStationManagerImpl(stations: List[Station]) extends CheckedStationManager:
    type StationMapType = List[Station]
    type E              = CheckedStationManager.Error

    def addStation(station: Station): R =
      val updatedStations = station :: stations
      (
        validateUniqueItems(
          updatedStations.map(_.name),
          CheckedStationManager.Error.DuplicateStationName
        ).toValidatedNec,
        validateUniqueItems(
          updatedStations.map(_.coordinate),
          CheckedStationManager.Error.DuplicateStationLocation
        ).toValidatedNec
      )
        .mapN((_, _) => CheckedStationManagerImpl(updatedStations)).toEither

    def removeStation(station: Station): R =
      if stations.contains(station) then
        Right(CheckedStationManagerImpl(stations.filterNot(_ == station)))
      else
        Left(NonEmptyChain(CheckedStationManager.Error.StationNotFound))

    def findStationAt(coordinate: Coordinate): Option[Station] =
      stations.find(_.coordinate == coordinate)

    export stations.map

  private final case class UncheckedStationManagerImpl(
      stations: List[Station]
  ) extends UncheckedStationManager:
    type StationMapType = List[Station]

    def addStation(station: Station): R =
      UncheckedStationManagerImpl(station :: stations)

    def removeStation(station: Station): R =
      UncheckedStationManagerImpl(stations.filterNot(_ == station))

    def findStationAt(coordinate: Coordinate): Option[Station] =
      stations.find(_.coordinate == coordinate)

    export stations.map

  object CheckedStationManager:
    /** Represents errors that can occur during [[CheckedStationManager]] creation. */
    enum Error extends BaseError:
      case DuplicateStationName, DuplicateStationLocation, StationNotFound
