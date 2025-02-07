package ulisse.entities.station

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

class StationTest extends AnyWordSpec with Matchers:

  "A Station" should:
    "be created if the name is not blank and numberOfTracks is greater than 0" in:
      Station.createCheckedStation("name", Coordinate(0, 0), 1) shouldBe a[Right[_, _]]

    "not be created if the name is blank" in:
      List("", "  ").foreach(invalidName =>
        Station.createCheckedStation(invalidName, Coordinate(0, 0), 1) shouldBe Left(
          Chain(Station.CheckedStation.Error.InvalidName)
        )
      )

    "not be created if capacity is less than or equal to 0" in:
      List(-1, 0).foreach(invalidNumberOfTrack =>
        Station.createCheckedStation(
          "name",
          Coordinate(0, 0),
          invalidNumberOfTrack
        ) shouldBe Left(Chain(Station.CheckedStation.Error.InvalidNumberOfTrack))
      )
