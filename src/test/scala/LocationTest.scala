import model.station.Location
import org.scalatest.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LocationTest extends AnyFlatSpec with Matchers:

  "A Location" should "not throw an exception if latitude is between -90 and 90" in:
    noException should be thrownBy:
      Location(45.0, 0.0)

  it should "throw an IllegalArgumentException if latitude is greater than 90 or less than -90" in:
    intercept[IllegalArgumentException]:
      Location(91.0, 0.0)
    intercept[IllegalArgumentException]:
      Location(-91.0, 0.0)

  it should "not throw an exception if longitude is between -180 and 180" in:
    noException should be thrownBy:
      Location(0.0, 90.0)

  it should "throw an IllegalArgumentException if longitude is greater than 180 or less than -180" in:
    intercept[IllegalArgumentException]:
      Location(0.0, 181.0)
    intercept[IllegalArgumentException]:
      Location(0.0, -181.0)
