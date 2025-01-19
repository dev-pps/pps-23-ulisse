package ulisse.applications.station

import cats.implicits.catsSyntaxEq
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

/** Defines a map of stations.
  *
  * A StationMap represents a collection of stations.
  *
  * **Requirements**:
  *   - The `name` of each station must be unique.
  *   - The `location` of each station must be unique.
  *
  * @tparam L
  *   The type of the location associated with the station.
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
  enum Error:
    case DuplicateStationName, DuplicateStationLocation, StationNotFound

  private def validateUniqueNames[N: Numeric, C <: Coordinate[N], T <: Seq[Station[N, C]]](
      stations: T
  ): Either[Error, Unit] =
    Either.cond(
      stations.map(_.name).distinct.size === stations.size,
      (),
      Error.DuplicateStationName
    )

  private def validateUniqueLocations[N: Numeric, C <: Coordinate[N], T <: Seq[Station[N, C]]](
      stations: T
  ): Either[Error, Unit] =
    Either.cond(
      stations.map(_.location).distinct.size === stations.size,
      (),
      Error.DuplicateStationLocation
    )

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
        _ <- validateUniqueNames[N, C, StationMapType](updatedStations)
        _ <- validateUniqueLocations[N, C, StationMapType](updatedStations)
      yield StationMapImpl(updatedStations)

    def removeStation(station: Station[N, C]): Either[Error, StationMap[N, C]] =
      if stations.exists(_.location === station.location) then
        Right(
          StationMapImpl(stations.filterNot(_.location === station.location))
        )
      else
        Left(Error.StationNotFound)

    def findStationAt(location: C): Option[Station[N, C]] =
      stations.find(_.location === location)

    export stations.map
