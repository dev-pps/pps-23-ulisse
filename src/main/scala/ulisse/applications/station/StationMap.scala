package ulisse.applications.station

import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.validateUniqueItems

/** Defines a map of stations.
  *
  * A `StationMap` is a collection of `Station` instances.
  *
  * @tparam N
  *   The numeric type representing the coordinates of the station (e.g., `Int`, `Double`).
  * @tparam C
  *   A type that extends `Coordinate[N]`, which represents the station's location.
  */
trait StationMap[N: Numeric, C <: Coordinate[N], S <: Station[N, C]]:

  /** The type representing the collection of stations. */
  type StationMapType <: Seq[S]

  /** The return type of the methods for adding or removing stations. */
  type R

  /** The collection of stations in the map. */
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
    case that: StationMap[_, _, _] => that.stations == stations
    case _                         => false

  /** Adds a station to the map.
    *
    * @param station
    *   The station to add.
    * @return
    *   Either a new `StationMap` with the added station or an `Error` indicating the issue.
    */
  def addStation(station: S): R

  /** Removes a station from the map.
    *
    * @param station
    *   The station to remove.
    * @return
    *   Either a new `StationMap` without the removed station or an `Error` indicating the issue.
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

/** Factory for [[StationMap]] instances. */
object StationMap:
  /** Creates a `StationMap` instance.
    *
    * @tparam N
    *   The numeric type representing the coordinates of the stations (e.g., `Int`, `Double`).
    * @tparam C
    *   A type that extends `Coordinate[N]`, representing the station's location.
    * @return
    *   A `StationMap` instance.
    */
  def apply[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](stations: S*): StationMap[N, C, S] =
    BaseStationMap(stations.toList)

  /** Creates a `CheckedStationMap` instance, which is a `StationMap` with validation for unique names and locations.
    *
    * @tparam N
    *   The numeric type representing the coordinates of the stations (e.g., `Int`, `Double`).
    * @tparam C
    *   A type that extends `Coordinate[N]`, representing the station's location.
    * @return
    *   An empty `CheckedStationMap` instance.
    */
  def createCheckedStationMap[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](): CheckedStationMap[N, C, S] =
    CheckedStationMap(List.empty)

  /** A case class that implements the `StationMap` trait with validation for unique station names and locations.
    *
    * @tparam N
    *   The numeric type representing the coordinates of the stations (e.g., `Int`, `Double`).
    * @tparam C
    *   A type that extends `Coordinate[N]`, representing the station's location.
    * @param stations
    *   The list of stations in the map. **Note**: Instances of `Grid` can only be created through the
    *   `Coordinates.createGrid` method to ensure validation.
    */
  final case class CheckedStationMap[N: Numeric, C <: Coordinate[N], S <: Station[N, C]] private[StationMap] (
      stations: List[S]
  ) extends StationMap[N, C, S]:
    type StationMapType = List[S]
    type R              = Either[CheckedStationMap.Error, CheckedStationMap[N, C, S]]

    def addStation(station: S): R =
      val updatedStations = station :: stations
      for
        _ <- validateUniqueItems(updatedStations.map(_.name), CheckedStationMap.Error.DuplicateStationName)
        _ <- validateUniqueItems(updatedStations.map(_.coordinate), CheckedStationMap.Error.DuplicateStationLocation)
      yield CheckedStationMap(updatedStations)

    def removeStation(station: S): R =
      if stations.exists(_.coordinate === station.coordinate) then
        Right(CheckedStationMap(stations.filterNot(_.coordinate === station.coordinate)))
      else
        Left(CheckedStationMap.Error.StationNotFound)

    def findStationAt(coordinate: C): Option[S] =
      stations.find(_.coordinate === coordinate)

    export stations.map

  private final case class BaseStationMap[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](
      stations: List[S]
  ) extends StationMap[N, C, S]:
    type StationMapType = List[S]
    type R              = BaseStationMap[N, C, S]

    def addStation(station: S): R =
      BaseStationMap(station :: stations)

    def removeStation(station: S): R =
      BaseStationMap(stations.filterNot(_.coordinate === station.coordinate))

    def findStationAt(coordinate: C): Option[S] =
      stations.find(_.coordinate === coordinate)

    export stations.map

  object CheckedStationMap:
    /** Represents errors that can occur during [[CheckedStationMap]] creation. */
    enum Error extends BaseError:
      case DuplicateStationName, DuplicateStationLocation, StationNotFound
