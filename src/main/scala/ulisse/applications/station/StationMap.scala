package applications.station

import cats.implicits.catsSyntaxEq
import entities.Location
import entities.station.Station

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
trait StationMap[L <: Location]:
  type StationMapType <: Seq[Station[L]]
  val stations: StationMapType

  /** Adds a station to the map.
    *
    * @param station
    *   The station to add.
    * @return
    *   Either a `StationMap` instance with the added station or an 'Error'
    *   indicating the issue.
    */
  def addStation(station: Station[L]): Either[StationMap.Error, StationMap[L]]

  /** Removes a station from the map.
    *
    * @param station
    *   The station to remove.
    * @return
    *   Either a `StationMap` instance without the removed station or an 'Error'
    *   indicating the issue.
    */
  def removeStation(station: Station[L])
      : Either[StationMap.Error, StationMap[L]]

  /** Finds a station at the given location.
    *
    * @param location
    *   The location to search for.
    * @return
    *   An `Option` containing the station at the given location, if found.
    */
  def findStationAt(location: L): Option[Station[L]]

/** Factory for [[StationMap]] instances. */
object StationMap:

  /** Represents errors that can occur during station map creation. */
  enum Error:
    case DuplicateStationName, DuplicateStationLocation, StationNotFound

  private def validateUniqueNames[L <: Location, T <: Seq[Station[L]]](
      stations: T
  ): Either[Error, Unit] =
    Either.cond(
      stations.map(_.name).distinct.size === stations.size,
      (),
      Error.DuplicateStationName
    )

  private def validateUniqueLocations[L <: Location, T <: Seq[Station[L]]](
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

  def apply[L <: Location](): StationMap[L] = StationMapImpl(List.empty)

  private final case class StationMapImpl[L <: Location](
      stations: List[Station[L]]
  ) extends StationMap[L]:
    type StationMapType = List[Station[L]]

    def addStation(station: Station[L]): Either[Error, StationMap[L]] =
      val updatedStations = station :: stations
      for
        _ <- validateUniqueNames[L, StationMapType](updatedStations)
        _ <- validateUniqueLocations[L, StationMapType](updatedStations)
      yield StationMapImpl(updatedStations)

    def removeStation(station: Station[L]): Either[Error, StationMap[L]] =
      if stations.exists(_.location === station.location) then
        Right(
          StationMapImpl(stations.filterNot(_.location === station.location))
        )
      else
        Left(Error.StationNotFound)

    def findStationAt(location: L): Option[Station[L]] =
      stations.find(_.location === location)
