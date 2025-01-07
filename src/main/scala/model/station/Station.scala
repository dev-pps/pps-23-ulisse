package model.station

import model.station.Location.*

trait Station:
  val name: String
  require(!name.isBlank, "name must not be empty or blank")
  val location: Location
  val numberOfTrack: Int
  require(numberOfTrack > 0, "numberOfTrack must be greater than 0")

object Station:
  def apply(name: String, location: Location, numberOfTrack: Int): Station =
    StationImpl(name, location, numberOfTrack)

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
