import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class WorldMapTest extends AnyWordSpec with Matchers:

  val stations: List[Station] = List(
    Station("StationA", Location(0.0, 0.0), 100),
    Station("StationB", Location(1.0, 1.0), 100),
    Station("StationC", Location(2.0, 2.0), 100)
  )

  "A WorldMap" when:
    "created with unique station names and locations" should:
      "be created successfully" in:
        noException should be thrownBy:
          StationMap(stations)

    "created with duplicate station names" should:
      "throw an IllegalArgumentException" in:
        intercept[IllegalArgumentException] {
          StationMap(
            Station("StationA", Location(3.0, 3.0), 100) +: stations
          )
        }

    "created with duplicate station locations" should:
      "throw an IllegalArgumentException" in:
        intercept[IllegalArgumentException] {
          StationMap(
            Station("StationD", Location(2.0, 2.0), 100) +: stations
          )
        }
