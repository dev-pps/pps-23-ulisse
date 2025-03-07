package ulisse.entities.station

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinate
import ulisse.entities.station.Station.minNumberOfPlatforms
import ulisse.entities.station.StationTest.{defaultNumberOfPlatform, makeStation, stationA}
object StationTest:
  val defaultNumberOfPlatform = 2
  val stationA                = makeStation("A", Coordinate(0, 0))
  val stationB                = makeStation("B", Coordinate(0, 1))
  val stationC                = makeStation("C", Coordinate(0, 2))
  val stationD                = makeStation("D", Coordinate(0, 3))
  val stationE                = makeStation("E", Coordinate(0, 4))
  val stationF                = makeStation("F", Coordinate(0, 5))
  def makeStation(name: String, coordinate: Coordinate): Station =
    Station(name, coordinate, defaultNumberOfPlatform)

class StationTest extends AnyWordSpec with Matchers:
  private val defaultName       = "name"
  private val defaultCoordinate = Coordinate(0, 0)

  "A Station" when:
    "is created" should:
      "have at least minNumberOfPlatforms" in:
        List(-2, -1, 0, 1, 2).map(minNumberOfPlatforms + _).foreach(numberOfTrack =>
          val station = Station(defaultName, defaultCoordinate, numberOfTrack)
          station.id shouldBe station.hashCode()
          station.name shouldBe defaultName
          station.coordinate shouldBe defaultCoordinate
          station.numberOfPlatforms shouldBe math.max(minNumberOfPlatforms, numberOfTrack)
        )

    "is created checked" should:
      "be created if the name is not blank and numberOfPlatforms is at least minNumberOfPlatforms" in:
        Station.createCheckedStation(defaultName, defaultCoordinate, defaultNumberOfPlatform) shouldBe Right(Station(
          defaultName,
          defaultCoordinate,
          defaultNumberOfPlatform
        ))

      "not be created if the name is blank" in:
        List("", "  ").foreach(invalidName =>
          Station.createCheckedStation(invalidName, defaultCoordinate, defaultNumberOfPlatform) shouldBe Left(
            Chain(Station.Error.InvalidName)
          )
        )

      "not be created if numberOfPlatforms is lower than minNumberOfPlatforms" in:
        List(-2, -1).map(minNumberOfPlatforms + _).foreach(invalidNumberOfTrack =>
          Station.createCheckedStation(
            defaultName,
            defaultCoordinate,
            invalidNumberOfTrack
          ) shouldBe Left(Chain(Station.Error.InvalidNumberOfPlatforms))
        )

      "return the chain of error if both name and numberOfPlatforms are invalid" in:
        Station.createCheckedStation("", defaultCoordinate, 0) shouldBe Left(
          Chain(Station.Error.InvalidName, Station.Error.InvalidNumberOfPlatforms)
        )

    "is made" should:
      "be equal a station with defaultNumberOfPlatforms" in:
        makeStation(defaultName, defaultCoordinate) shouldBe Station(
          defaultName,
          defaultCoordinate,
          defaultNumberOfPlatform
        )

    "coordinate is changed" should:
      "be equal to a station with the new coordinate" in:
        stationA.withCoordinate(defaultCoordinate + Coordinate(1, 1)) shouldBe Station(
          stationA.name,
          stationA.coordinate + Coordinate(1, 1),
          stationA.numberOfPlatforms
        )

    "it is equaled" should:
      "be equal to itself" in:
        stationA shouldBe stationA

      "not be equal to a station with different name" in:
        stationA should not be Station(stationA.name+"2", stationA.coordinate, stationA.numberOfPlatforms)

      "not be equal to a station with different coordinate" in:
        stationA should not be Station(stationA.name, stationA.coordinate + Coordinate(1, 1), stationA.numberOfPlatforms)

      "not be equal to a station with different number of platforms" in:
        stationA should not be Station(stationA.name, stationA.coordinate, stationA.numberOfPlatforms + 1)

      "have as id the hash of its coordinate" in:
        stationA.id shouldBe stationA.coordinate.##