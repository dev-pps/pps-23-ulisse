import model.station.{Location, Station}
import org.scalatest.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class StationTest extends AnyWordSpec with Matchers:

  "A Station" when:
    "name is inserted" should:
      "not throw an IllegalArgumentException" in:
        noException should be thrownBy:
          Station("name", Location(45.0, 90.0), 100)

    "name is empty" should:
      "throw an IllegalArgumentException" in:
        intercept[IllegalArgumentException]:
          Station("", Location(45.0, 90.0), 100)

    "name is blank" should:
      "throw an IllegalArgumentException" in:
        intercept[IllegalArgumentException]:
          Station("   ", Location(45.0, 90.0), 100)

    "capacity is greater than 0" should:
      "not throw an IllegalArgumentException" in:
        noException should be thrownBy:
          Station("name", Location(45.0, 90.0), 1)

    "capacity is less than or equal to 0" should:
      "throw an IllegalArgumentException" in:
        intercept[IllegalArgumentException]:
          Station("name", Location(45.0, 90.0), 0)
        intercept[IllegalArgumentException]:
          Station("name", Location(45.0, 90.0), -1)
