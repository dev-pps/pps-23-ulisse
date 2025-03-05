package ulisse.applications.managers

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinate
import ulisse.entities.station.Station
import ulisse.entities.station.StationTest.stationA

class StationManagerTest extends AnyWordSpec with Matchers:
  "A StationManager" when:
    "created" should:
      "be empty" in:
        StationManager().stations shouldBe empty

    "a new station is added" should:
      "contain the new station" in:
        StationManager().addStation(stationA).map(_.stations) match
          case Right(stations) =>
            stations should contain only stationA
          case Left(_) => fail()

    "another station with same name is added" should:
      "not be added and returns error" in:
        val otherStation = Station(stationA.name, stationA.coordinate + Coordinate(1, 1), stationA.numberOfPlatforms)
        StationManager().addStation(stationA).flatMap(
          _.addStation(otherStation)
        ) shouldBe Left(Chain(StationManager.Error.DuplicateStationName))

    "another station with same location is added" should:
      "not be added and returns error" in:
        val otherStation = Station(stationA.name + "2", stationA.coordinate, stationA.numberOfPlatforms)
        StationManager().addStation(stationA).flatMap(
          _.addStation(otherStation)
        ) shouldBe Left(Chain(StationManager.Error.DuplicateStationLocation))

    "the same station is added" should:
      "not be added and return the chain of errors" in:
        StationManager().addStation(stationA).flatMap(
          _.addStation(stationA)
        ) shouldBe Left(
          Chain(
            StationManager.Error.DuplicateStationName,
            StationManager.Error.DuplicateStationLocation
          )
        )

    "existing station is removed" should:
      "no longer be present" in:
        StationManager().addStation(stationA).flatMap(
          _.removeStation(stationA)
        ).map(_.stations) match
          case Right(stations) => stations should not contain stationA
          case Left(_)         => fail()

    "non-existing station is removed" should:
      "return error" in:
        StationManager().removeStation(stationA) shouldBe Left(
          Chain(StationManager.Error.StationNotFound)
        )

    "a station is searched by location" should:
      "return the station if it exists" in:
        StationManager().addStation(stationA).map(
          _.findStationAt(stationA.coordinate)
        ) shouldBe Some(stationA)

      "return None if the station does not exist" in:
        StationManager().findStationAt(stationA.coordinate) shouldBe None
