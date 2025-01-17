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
  * @tparam T
  *   The type of the collection of stations.
  */
trait StationMap[L <: Location, T <: List[Station[L]]]:
  val stations: T

/** Factory for [[StationMap]] instances. */
object StationMap:

  /** Represents errors that can occur during station map creation. */
  enum Error:
    case DuplicateStationName, DuplicateStationLocation

  private def validateUniqueNames[L <: Location, T <: List[Station[L]]](
      stations: T
  ): Either[Error, Unit] =
    Either.cond(
      stations.map(_.name).distinct.size === stations.size,
      (),
      Error.DuplicateStationName
    )

  private def validateUniqueLocations[L <: Location, T <: List[Station[L]]](
      stations: T
  ): Either[Error, Unit] =
    Either.cond(
      stations.map(_.location).distinct.size === stations.size,
      (),
      Error.DuplicateStationLocation
    )

  /** Creates a `StationMap` instance with validation.
    *
    * @param stations
    *   The collection of stations.
    * @tparam L
    *   The type of the location associated with the station.
    * @tparam T
    *   The type of the collection of stations.
    * @return
    *   Either a `StationMap` instance or an `Error` indicating the issue.
    */
  def apply[L <: Location, T <: List[Station[L]]](stations: T)
      : Either[Error, StationMap[L, T]] =
    for
      a <- validateUniqueNames(stations)
      _ = print(a)
      b <- validateUniqueLocations(stations)
      _ = print(b)
    yield StationMapImpl(stations)
  private final case class StationMapImpl[L <: Location, T <: List[Station[L]]](
      stations: T
  ) extends StationMap[L, T]
