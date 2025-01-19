package ulisse.applications.station

import cats.implicits.catsSyntaxEq
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station
import ulisse.utils.Errors.AppError
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
  val stations: StationMapType
  def map[B](f: Station[N, C] => B): List[B]

  /** Adds a station to the map.
    *
    * @param station
    *   The station to add.
    * @return
    *   Either a `StationMap` instance with the added station or an 'Error' indicating the issue.
    */
  def addStation(station: Station[N, C]): Either[StationMap.Error, StationMap[N, C]]

  /** Removes a station from the map.
    *
    * @param station
    *   The station to remove.
    * @return
    *   Either a `StationMap` instance without the removed station or an 'Error' indicating the issue.
    */
  def removeStation(station: Station[N, C]): Either[StationMap.Error, StationMap[N, C]]

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
  enum Error extends AppError:
    case DuplicateStationName, DuplicateStationLocation, StationNotFound

  /** Creates an empty `StationMap` instance.
    *
    * @tparam L
    *   The type of the location associated with the station.
    * @return
    *   A `StationMap` instance.
    */

  def apply[N: Numeric, C <: Coordinate[N]](): StationMap[N, C] = StationMapImpl(List.empty)

  private final case class StationMapImpl[N: Numeric, C <: Coordinate[N]](
      stations: List[Station[N, C]]
  ) extends StationMap[N, C]:
    type StationMapType = List[Station[N, C]]

    def addStation(station: Station[N, C]): Either[Error, StationMap[N, C]] =
      val updatedStations = station :: stations
      for
        _ <- validateUniqueItems(updatedStations.map(_.name), Error.DuplicateStationName)
        _ <- validateUniqueItems(updatedStations.map(_.location), Error.DuplicateStationLocation)
      yield StationMapImpl(updatedStations)

    def removeStation(station: Station[N, C]): Either[Error, StationMap[N, C]] =
      if stations.exists(_.location === station.location) then
        Right(
          StationMapImpl(stations.filterNot(_.location === station.location))
        )
      else
        Left(Error.StationNotFound)

    def findStationAt(coordinate: C): Option[Station[N, C]] =
      stations.find(_.location === coordinate)

    export stations.map
