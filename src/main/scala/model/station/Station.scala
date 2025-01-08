package model.station

import model.station.Location.*

/** Defines a Station. */
trait Station:
  /** The name of the station. Must not be empty or blank. */
  val name: String
  require(!name.isBlank, "name must not be empty or blank")
  val location: Location

  /** The number of tracks of the station. Must be greater than 0. */
  val numberOfTrack: Int
  require(numberOfTrack > 0, "numberOfTrack must be greater than 0")

/** Factory for [[Station]] instances. */
object Station:
  def apply(name: String, location: Location, numberOfTrack: Int): Station =
    StationImpl(name, location, numberOfTrack)

/** Defines a Selectable Object. */
trait Selectable:
  val selected: Boolean

final case class StationImpl(
    name: String,
    location: Location,
    numberOfTrack: Int
) extends Station

final case class SelectableStation(station: Station, selected: Boolean)
    extends Station with Selectable:
  export station.*
