object Location:
  opaque type Location = (Double, Double)
  def apply(latitude: Double, longitude: Double): Location =
    require(
      latitude >= -90.0 && latitude <= 90.0,
      s"invalid latitude: $latitude"
    )
    require(
      longitude >= -180.0 && longitude <= 180.0,
      s"invalid longitude: $longitude"
    )
    (latitude, longitude)
import Location.*

trait Station:
  val name: String
  val location: Location
  val capacity: Int

trait Selectable:
  val selected: Boolean

final case class StationImpl(name: String, location: Location, capacity: Int)
    extends Station
final case class SelectableStation(station: Station, selected: Boolean)
    extends Station with Selectable:
  export station.*
final case class WorldMap(stations: List[SelectableStation])

val stations: List[Station] = List(
  StationImpl("A", Location(1.0, 1.0), 10),
  StationImpl("B", Location(2.0, 2.0), 20),
  StationImpl("C", Location(3.0, 3.0), 30),
  StationImpl("D", Location(4.0, 4.0), 40),
  StationImpl("E", Location(5.0, 5.0), 50)
)

val worldMap: WorldMap = WorldMap(stations.map(SelectableStation(_, false)))
