package ulisse.applications.managers

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.applications.managers.StationManager.CheckedStationManager
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.validateUniqueItems

/** Defines a manager for stations.
  *
  * A `StationManager` is a collection of `Station` instances.
  *
  * @tparam N
  *   The numeric type representing the coordinates of the station (e.g., `Int`, `Double`).
  * @tparam C
  *   A type that extends `Coordinate[N]`, which represents the station's location.
  */
trait StationManager[S <: Station[?]]:

  /** The type representing the collection of stations. */
  type StationMapType <: Seq[S]

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
  def map[B](f: S => B): List[B]

  /** Compares two `StationMap` instances for equality.
    *
    * @param other
    *   The other object to compare with.
    * @return
    *   `true` if both station maps have the same stations, `false` otherwise.
    */
  override def equals(other: Any): Boolean = other match
    case that: StationManager[_] => that.stations == stations
    case _                       => false

  /** Adds a station to the manager.
    *
    * @param station
    *   The station to add.
    * @return
    *   R type
    */
  def addStation(station: S): R

  /** Removes a station from the manager.
    *
    * @param station
    *   The station to remove.
    * @return
    *   R type
    */
  def removeStation(station: Station[?]): R

  /** Finds a station at the given location.
    *
    * @param coordinate
    *   The location to search for.
    * @return
    *   An `Option` containing the station at the given location, if found.
    */
  def findStationAt(coordinate: Coordinate[?]): Option[S]

trait UncheckedStationManager[S <: Station[?]] extends StationManager[S]:
  final type R = UncheckedStationManager[S]

trait CheckedStationManager[S <: Station[?]] extends StationManager[S]:
  final type R = Either[NonEmptyChain[E], CheckedStationManager[S]]
  type E       = CheckedStationManager.Error

/** Factory for [[StationManager]] instances. */
object StationManager:
  /** Creates a `StationManager` instance.
    *
    * @tparam N
    *   The numeric type representing the coordinates of the stations (e.g., `Int`, `Double`).
    * @tparam C
    *   A type that extends `Coordinate[N]`, representing the station's location.
    * @return
    *   A `StationManager` instance.
    */
  def apply[S <: Station[?]](stations: S*): UncheckedStationManager[S] =
    UncheckedStationManagerImpl(stations.toList)

  /** Creates a `CheckedStationManager` instance, which is a `StationManager` with validation for unique names and
    * locations.
    *
    * @tparam N
    *   The numeric type representing the coordinates of the stations (e.g., `Int`, `Double`).
    * @tparam C
    *   A type that extends `Coordinate[N]`, representing the station's location.
    * @return
    *   An empty `CheckedStationManager` instance.
    */
  def createCheckedStationManager[S <: Station[?]](): CheckedStationManager[S] =
    CheckedStationManagerImpl(List.empty)

  /** A case class that implements the `StationManager` trait with validation for unique station names and locations.
    *
    * @tparam N
    *   The numeric type representing the coordinates of the stations (e.g., `Int`, `Double`).
    * @tparam C
    *   A type that extends `Coordinate[N]`, representing the station's location.
    * @param stations
    *   The list of stations in the map. **Note**: Instances of `CheckedStationManager` can only be created through the
    *   `StationManager.createCheckedStationManager` method to ensure validation.
    */
  final case class CheckedStationManagerImpl[S <: Station[?]] private[StationManager] (
      stations: List[S]
  ) extends CheckedStationManager[S]:
    type StationMapType = List[S]
    type E              = CheckedStationManager.Error

    def addStation(station: S): R =
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

    def removeStation(station: Station[?]): R =
      if stations.contains(station) then
        Right(CheckedStationManagerImpl(stations.filterNot(_ == station)))
      else
        Left(NonEmptyChain(CheckedStationManager.Error.StationNotFound))

    def findStationAt(coordinate: Coordinate[?]): Option[S] =
      stations.find(_.coordinate == coordinate)

    export stations.map

  private final case class UncheckedStationManagerImpl[S <: Station[?]](
      stations: List[S]
  ) extends UncheckedStationManager[S]:
    type StationMapType = List[S]

    def addStation(station: S): R =
      UncheckedStationManagerImpl(station :: stations)

    def removeStation(station: Station[?]): R =
      UncheckedStationManagerImpl(stations.filterNot(_ == station))

    def findStationAt(coordinate: Coordinate[?]): Option[S] =
      stations.find(_.coordinate == coordinate)

    export stations.map

  object CheckedStationManager:
    /** Represents errors that can occur during [[CheckedStationManager]] creation. */
    enum Error extends BaseError:
      case DuplicateStationName, DuplicateStationLocation, StationNotFound
