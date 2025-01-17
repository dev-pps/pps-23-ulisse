package ulisse.entities

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Location

class LocationTest extends AnyWordSpec with Matchers:

  "A Location" when:
    "is a Geo" should:
      "be created if latitude is between -90 and 90 and longitude between -180 and 180" in:
        Location.createGeo(0.0, 0.0).map(location =>
          (location.latitude, location.longitude)
        ) shouldBe Right(0.0, 0.0)

      "not be created if latitude is greater than 90 or less than -90" in:
        Location.createGeo(-91.0, 0.0) shouldBe Left(
          Location.Error.InvalidLatitude
        )
        Location.createGeo(91.0, 0.0) shouldBe Left(
          Location.Error.InvalidLatitude
        )

      "not be created if longitude is greater than 180 or less than -180" in:
        Location.createGeo(0.0, -181.0) shouldBe Left(
          Location.Error.InvalidLongitude
        )
        Location.createGeo(0.0, 181.0) shouldBe Left(
          Location.Error.InvalidLongitude
        )

    "is a Grid" should:
      "be created if row and column are non-negative" in:
        Location.createGrid(0, 0).map(location =>
          (location.row, location.column)
        ) shouldBe Right((0, 0))

      "not be created if row is negative" in:
        Location.createGrid(-1, 0) shouldBe Left(Location.Error.InvalidRow)

      "not be created if column is negative" in:
        Location.createGrid(0, -1) shouldBe Left(Location.Error.InvalidColumn)
