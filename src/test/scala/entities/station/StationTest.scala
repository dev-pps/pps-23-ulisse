package entities.station

import entities.Location
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class StationTest extends AnyWordSpec with Matchers:

  "A Station" should:
    "be created if the name is not blank and numberOfTracks is greater than 0" in:
      (for
        location <- Location.createGrid(0, 0)
        station  <- Station("name", location, 100)
      yield (station.name, station.numberOfTrack)) shouldBe Right("name", 100)

    "not be created if the name is blank" in:
      List("", "  ").foreach(invalidName =>
        (for
          location <- Location.createGrid(0, 0)
          station  <- Station(invalidName, location, 100)
        yield (station.name, station.numberOfTrack)) shouldBe Left(
          Station.Error.InvalidName
        )
      )

    "not be created if capacity is less than or equal to 0" in:
      List(0, -1).foreach(invalidNumberOfTrack =>
        (for
          location <- Location.createGrid(0, 0)
          station  <- Station("name", location, invalidNumberOfTrack)
        yield (station.name, station.numberOfTrack)) shouldBe Left(
          Station.Error.InvalidNumberOfTrack
        )
      )
