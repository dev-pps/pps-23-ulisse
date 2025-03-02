package ulisse.entities.station

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinate
import ulisse.entities.station.StationTest.numberOfTracks
object StationTest:
  val numberOfTracks = 2
  val stationA       = makeStation("A", Coordinate(0, 0))
  val stationB       = makeStation("B", Coordinate(0, 1))
  val stationC       = makeStation("C", Coordinate(0, 2))
  val stationD       = makeStation("D", Coordinate(0, 3))
  val stationE       = makeStation("E", Coordinate(0, 4))
  val stationF       = makeStation("F", Coordinate(0, 5))
  def makeStation(name: String, coordinate: Coordinate): Station =
    Station(name, coordinate, numberOfTracks)

class StationTest extends AnyWordSpec with Matchers:

  private val defaultName       = "name"
  private val defaultCoordinate = Coordinate(0, 0)

  "A Station" when:
    "is created" should:
      "have at least 1 track" in:
        List(-1, 0, 1, 2).foreach(numberOfTrack =>
          val station = Station(defaultName, defaultCoordinate, numberOfTrack)
          station.id shouldBe station.hashCode()
          station.name shouldBe defaultName
          station.coordinate shouldBe defaultCoordinate
          station.numberOfTracks shouldBe math.max(1, numberOfTrack)
        )

    "is checked" should:
      "be created if the name is not blank and numberOfTracks is greater than 0" in:
        Station.createCheckedStation(defaultName, defaultCoordinate, numberOfTracks) shouldBe Right(Station(
          defaultName,
          defaultCoordinate,
          numberOfTracks
        ))

      "not be created if the name is blank" in:
        List("", "  ").foreach(invalidName =>
          Station.createCheckedStation(invalidName, defaultCoordinate, numberOfTracks) shouldBe Left(
            Chain(Station.Error.InvalidName)
          )
        )

      "not be created if capacity is less than or equal to 0" in:
        List(-1, 0).foreach(invalidNumberOfTrack =>
          Station.createCheckedStation(
            defaultName,
            defaultCoordinate,
            invalidNumberOfTrack
          ) shouldBe Left(Chain(Station.Error.InvalidNumberOfTrack))
        )

      "return the chain of error" in:
        Station.createCheckedStation("", defaultCoordinate, 0) shouldBe Left(
          Chain(Station.Error.InvalidName, Station.Error.InvalidNumberOfTrack)
        )
