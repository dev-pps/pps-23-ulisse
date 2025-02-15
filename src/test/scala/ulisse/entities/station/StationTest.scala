package ulisse.entities.station

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinate

class StationTest extends AnyWordSpec with Matchers:

  "A Station" when:
    "is created" should:
      List(-1, 0, 1, 2).foreach(numberOfTrack =>
        val station = Station("name", Coordinate(0, 0), numberOfTrack)
        station.name shouldBe "name"
        station.coordinate shouldBe Coordinate(0, 0)
        station.numberOfTracks shouldBe math.max(1, numberOfTrack)
      )

    "is checked" should:
      "be created if the name is not blank and numberOfTracks is greater than 0" in:
        Station.createNamedStation("name", Coordinate(0, 0), 1) shouldBe a[Right[_, _]]

      "not be created if the name is blank" in:
        List("", "  ").foreach(invalidName =>
          Station.createNamedStation(invalidName, Coordinate(0, 0), 1) shouldBe Left(
            Chain(Station.Error.InvalidName)
          )
        )

      "not be created if capacity is less than or equal to 0" in:
        List(-1, 0).foreach(invalidNumberOfTrack =>
          Station.createNamedStation(
            "name",
            Coordinate(0, 0),
            invalidNumberOfTrack
          ) shouldBe Left(Chain(Station.Error.InvalidNumberOfTrack))
        )

      "return the chain of error" in:
        Station.createNamedStation("", Coordinate(0, 0), 0) shouldBe Left(
          Chain(Station.Error.InvalidName, Station.Error.InvalidNumberOfTrack)
        )
