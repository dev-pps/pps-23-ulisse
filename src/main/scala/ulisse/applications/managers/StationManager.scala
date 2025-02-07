package ulisse.applications.managers

import cats.data.NonEmptyChain

import cats.syntax.all.*
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
trait StationManager[N: Numeric, C <: Coordinate[N], S <: Station[N, C]]:

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
    case that: StationManager[_, _, _] => that.stations == stations
    case _                             => false

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
  def removeStation(station: S): R

  /** Finds a station at the given location.
    *
    * @param coordinate
    *   The location to search for.
    * @return
    *   An `Option` containing the station at the given location, if found.
    */
  def findStationAt(coordinate: C): Option[S]

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
  def apply[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](stations: S*): StationManager[N, C, S] =
    BaseStationManager(stations.toList)

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
  def createCheckedStationManager[N: Numeric, C <: Coordinate[N], S <: Station[N, C]]()
      : CheckedStationManager[N, C, S] =
    CheckedStationManager(List.empty)

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
  final case class CheckedStationManager[N: Numeric, C <: Coordinate[N], S <: Station[N, C]] private[StationManager] (
      stations: List[S]
  ) extends StationManager[N, C, S]:
    type StationMapType = List[S]
    type R              = Either[NonEmptyChain[CheckedStationManager.Error], CheckedStationManager[N, C, S]]

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
        .mapN((_, _) => CheckedStationManager(updatedStations)).toEither

    def removeStation(station: S): R =
      if stations.exists(_.coordinate === station.coordinate) then
        Right(CheckedStationManager(stations.filterNot(_.coordinate === station.coordinate)))
      else
        Left(NonEmptyChain(CheckedStationManager.Error.StationNotFound))

    def findStationAt(coordinate: C): Option[S] =
      stations.find(_.coordinate === coordinate)

    export stations.map

  private final case class BaseStationManager[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](
      stations: List[S]
  ) extends StationManager[N, C, S]:
    type StationMapType = List[S]
    type R              = BaseStationManager[N, C, S]

    def addStation(station: S): R =
      BaseStationManager(station :: stations)

    def removeStation(station: S): R =
      BaseStationManager(stations.filterNot(_.coordinate === station.coordinate))

    def findStationAt(coordinate: C): Option[S] =
      stations.find(_.coordinate === coordinate)

    export stations.map

  object CheckedStationManager:
    /** Represents errors that can occur during [[CheckedStationManager]] creation. */
    enum Error extends BaseError:
      case DuplicateStationName, DuplicateStationLocation, StationNotFound
