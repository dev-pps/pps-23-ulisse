import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class WorldMapTest extends AnyWordSpec with Matchers:

  val stations: List[SelectableStation] = List(
    SelectableStation(Station("StationA", Location(0.0, 0.0), 100), false),
    SelectableStation(Station("StationB", Location(1.0, 1.0), 100), false),
    SelectableStation(Station("StationC", Location(2.0, 2.0), 100), false)
  )

  "A WorldMap" when:
    "created with unique station names and locations" should:
      "be created successfully" in:
        noException should be thrownBy:
          WorldMap(stations)

    "created with duplicate station names" should:
      "throw an IllegalArgumentException" in:
        intercept[IllegalArgumentException] {
          WorldMap(SelectableStation(
            Station("StationA", Location(3.0, 3.0), 100),
            false
          ) +: stations)
        }

    "created with duplicate station locations" should:
      "throw an IllegalArgumentException" in:
        intercept[IllegalArgumentException] {
          WorldMap(SelectableStation(
            Station("StationD", Location(2.0, 2.0), 100),
            false
          ) +: stations)
        }
