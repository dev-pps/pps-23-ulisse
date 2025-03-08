package ulisse.applications.managers

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.applications.managers.StationManager.Error.{DuplicateStationLocation, DuplicateStationName}
import ulisse.entities.Coordinate
import ulisse.entities.station.Station
import ulisse.entities.station.StationTest.{stationA, stationB}

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
        StationManager().addStation(stationA.withCoordinate(stationA.coordinate + Coordinate(1, 1))).flatMap(
          _.addStation(stationA)
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
        ).map(_.stations) shouldBe Right(List())

    "non-existing station is removed" should:
      "return error" in:
        StationManager().removeStation(stationA) shouldBe Left(
          Chain(StationManager.Error.StationNotFound)
        )

    "a station is updated with a new station" should:
      "replace the old station with the new one" in:
        StationManager().addStation(stationA).flatMap(
          _.updateStation(stationA, stationB)
        ).map(_.stations) shouldBe Right(List(stationB))

    "a station is updated with a the same station" should:
      "keep the station unchanged" in:
        StationManager().addStation(stationA).flatMap(
          _.updateStation(stationA, stationA)
        ).map(_.stations) shouldBe Right(List(stationA))

    "a station is updated with a station that is not in the manager" should:
      "return an error" in:
        StationManager().updateStation(stationA, stationB) shouldBe Left(
          Chain(StationManager.Error.StationNotFound)
        )

    "a station is updated with a station that is not accepted by the manager" should:
      "return an error" in:
        StationManager().addStation(stationA).flatMap(_.addStation(stationB)).flatMap(
          _.updateStation(stationA, stationB)
        ) shouldBe Left(Chain(DuplicateStationName, DuplicateStationLocation))

    "a station is searched by location" should:
      "return the station if it exists" in:
        StationManager().addStation(stationA).map(
          _.findStationAt(stationA.coordinate)
        ) shouldBe Right(Some(stationA))

      "return None if the station does not exist" in:
        StationManager().findStationAt(stationA.coordinate) shouldBe None
