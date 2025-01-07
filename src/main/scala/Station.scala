import Location.*

trait Station:
  val name: String
  require(!name.isBlank, "name must not be empty or blank")
  val location: Location
  val capacity: Int
  require(capacity > 0, "capacity must be greater than 0")

object Station:
  def apply(name: String, location: Location, capacity: Int): Station =
    StationImpl(name, location, capacity)

trait Selectable:
  val selected: Boolean

final case class StationImpl(name: String, location: Location, capacity: Int)
    extends Station

final case class SelectableStation(station: Station, selected: Boolean)
    extends Station with Selectable:
  export station.*
