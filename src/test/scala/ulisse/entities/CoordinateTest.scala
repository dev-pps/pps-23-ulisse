package ulisse.entities

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinates.*

class CoordinateTest extends AnyWordSpec with Matchers:

  "A Coordinate" when:
    "is a Geo" should:
      "be created if latitude is between -90 and 90 and longitude between -180 and 180" in:
        Coordinate.createGeo(0.0, 0.0).map(location =>
          (location.latitude, location.longitude)
        ) shouldBe Right(0.0, 0.0)

        Coordinate.createValidatedGeo(0.0, 0.0).map(location =>
          (location.latitude, location.longitude)
        ) shouldBe Right(0.0, 0.0)

      "not be created if latitude is greater than 90 or less than -90" in:
        Coordinate.createGeo(-91.0, 0.0) shouldBe Left(
          Geo.Error.InvalidLatitude
        )

        Coordinate.createValidatedGeo(-91.0, 0.0) shouldBe Left(
          Chain(Geo.Error.InvalidLatitude)
        )

        Coordinate.createGeo(91.0, 0.0) shouldBe Left(
          Geo.Error.InvalidLatitude
        )

        Coordinate.createValidatedGeo(91.0, 0.0) shouldBe Left(
          Chain(Geo.Error.InvalidLatitude)
        )

      "not be created if longitude is greater than 180 or less than -180" in:
        Coordinate.createGeo(0.0, -181.0) shouldBe Left(
          Geo.Error.InvalidLongitude
        )

        Coordinate.createValidatedGeo(0.0, -181.0) shouldBe Left(
          Chain(Geo.Error.InvalidLongitude)
        )

        Coordinate.createGeo(0.0, 181.0) shouldBe Left(
          Geo.Error.InvalidLongitude
        )

        Coordinate.createValidatedGeo(0.0, 181.0) shouldBe Left(
          Chain(Geo.Error.InvalidLongitude)
        )

      "return the chain of error" in:
        Coordinate.createValidatedGeo(91.0, 181.0) shouldBe Left(
          Chain(Geo.Error.InvalidLatitude, Geo.Error.InvalidLongitude)
        )

    "is a Grid" should:
      "be created if row and column are non-negative" in:
        Coordinate.createGrid(0, 0).map(location =>
          (location.row, location.column)
        ) shouldBe Right((0, 0))

        Coordinate.createValidatedGrid(0, 0).map(location =>
          (location.row, location.column)
        ) shouldBe Right((0, 0))

      "not be created if row is negative" in:
        Coordinate.createGrid(-1, 0) shouldBe Left(Grid.Error.InvalidRow)

        Coordinate.createValidatedGrid(-1, 0) shouldBe Left(Chain(Grid.Error.InvalidRow))

      "not be created if column is negative" in:
        Coordinate.createGrid(0, -1) shouldBe Left(Grid.Error.InvalidColumn)

        Coordinate.createValidatedGrid(0, -1) shouldBe Left(Chain(Grid.Error.InvalidColumn))

      "return the chain of error" in:
        Coordinate.createValidatedGrid(-1, -1) shouldBe Left(
          Chain(Grid.Error.InvalidRow, Grid.Error.InvalidColumn)
        )
