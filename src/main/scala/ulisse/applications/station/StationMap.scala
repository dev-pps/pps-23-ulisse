package ulisse.applications.station

import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.validateUniqueItems

/** Defines a map of stations.
  *
  * A `StationMap` represents a collection of stations, where each station is uniquely identified by its name and its
  * location. This trait provides functionality to store, retrieve, and manipulate a collection of stations.
  *
  * **Requirements**:
  *   - The `name` of each station must be unique.
  *   - The `location` of each station must be unique.
  *
  * @tparam N
  *   The numeric type representing the coordinates of the station (e.g., `Int`, `Double`).
  *   - An instance of `Numeric` must be available for the `N` type.
  * @tparam C
  *   A type that extends `Coordinate[N]`, which represents the station's location.
  *   - The `C` type must provide a way to compare coordinates and ensure uniqueness.
  */
trait StationMap[N: Numeric, C <: Coordinate[N]]:
  type StationMapType <: Seq[Station[N, C]]
  type R
  val stations: StationMapType
  def map[B](f: Station[N, C] => B): List[B]

  /** Adds a station to the map.
    *
    * @param station
    *   The station to add.
    * @return
    *   Either a `StationMap` instance with the added station or an 'Error' indicating the issue.
    */
  def addStation(station: Station[N, C]): R

  /** Removes a station from the map.
    *
    * @param station
    *   The station to remove.
    * @return
    *   Either a `StationMap` instance without the removed station or an 'Error' indicating the issue.
    */
  def removeStation(station: Station[N, C]): R

  /** Finds a station at the given location.
    *
    * @param location
    *   The location to search for.
    * @return
    *   An `Option` containing the station at the given location, if found.
    */
  def findStationAt(location: C): Option[Station[N, C]]

/** Factory for [[StationMap]] instances. */
object StationMap:

  /** Represents errors that can occur during station map creation. */
  enum Error extends BaseError:
    case DuplicateStationName, DuplicateStationLocation, StationNotFound

  /** Creates an empty `StationMap` instance.
    *
    * @tparam L
    *   The type of the location associated with the station.
    * @return
    *   A `StationMap` instance.
    */

  def apply[N: Numeric, C <: Coordinate[N]](): BaseStationMap[N, C]                      = BaseStationMap(List.empty)
  def createCheckedStationMap[N: Numeric, C <: Coordinate[N]](): CheckedStationMap[N, C] = CheckedStationMap(List.empty)
  final case class BaseStationMap[N: Numeric, C <: Coordinate[N]] private[StationMap] (
      stations: List[Station[N, C]]
  ) extends StationMap[N, C]:
    type StationMapType = List[Station[N, C]]
    type R              = BaseStationMap[N, C]

    def addStation(station: Station[N, C]): R =
      BaseStationMap(station :: stations)

    def removeStation(station: Station[N, C]): R =
      BaseStationMap(stations.filterNot(_.coordinate === station.coordinate))

    def findStationAt(coordinate: C): Option[Station[N, C]] =
      stations.find(_.coordinate === coordinate)

    export stations.map

  final case class CheckedStationMap[N: Numeric, C <: Coordinate[N]] private[StationMap] (
      stations: List[Station[N, C]]
  ) extends StationMap[N, C]:
    type StationMapType = List[Station[N, C]]
    type R              = Either[Error, CheckedStationMap[N, C]]

    def addStation(station: Station[N, C]): R =
      val updatedStations = station :: stations
      for
        _ <- validateUniqueItems(updatedStations.map(_.name), Error.DuplicateStationName)
        _ <- validateUniqueItems(updatedStations.map(_.coordinate), Error.DuplicateStationLocation)
      yield CheckedStationMap(updatedStations)

    def removeStation(station: Station[N, C]): R =
      if stations.exists(_.coordinate === station.coordinate) then
        Right(
          CheckedStationMap(stations.filterNot(_.coordinate === station.coordinate))
        )
      else
        Left(Error.StationNotFound)

    def findStationAt(coordinate: C): Option[Station[N, C]] =
      stations.find(_.coordinate === coordinate)

    export stations.map
