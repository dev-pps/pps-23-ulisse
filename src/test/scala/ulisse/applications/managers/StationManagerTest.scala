package ulisse.applications.managers

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.StationManager.CheckedStationManager
import ulisse.entities.Coordinates.*
import ulisse.entities.station.Station

class StationManagerTest extends AnyWordSpec with Matchers:

  private val station = Station("StationA", Coordinate(0, 0), 1)

  "A StationManager" when:
    "created" should:
      "be empty" in:
        StationManager.createCheckedStationManager[
          Station[Coordinate[Int]]
        ]().stations shouldBe empty

    "a new station is added" should:
      "contain the new station" in:
        StationManager.createCheckedStationManager().addStation(station).map(_.stations) match
          case Right(stations) =>
            stations should contain only station
          case Left(_) => fail()

    "another station with same name is added" should:
      "not be added and returns error" in:
        val otherStation = Station("StationA", Coordinate(1.2, 1), 1)
        StationManager.createCheckedStationManager().addStation(station).flatMap(
          _.addStation(otherStation)
        ) shouldBe Left(Chain(CheckedStationManager.Error.DuplicateStationName))

    "another station with same location is added" should:
      "not be added and returns error" in:
        val otherStation = Station("StationB", Coordinate(0, 0), 1)
        StationManager.createCheckedStationManager().addStation(station).flatMap(
          _.addStation(otherStation)
        ) shouldBe Left(Chain(CheckedStationManager.Error.DuplicateStationLocation))

    "the same station is added" should:
      "not be addend and return the chain of errors" in:
        StationManager.createCheckedStationManager().addStation(station).flatMap(
          _.addStation(station)
        ) shouldBe Left(
          Chain(
            CheckedStationManager.Error.DuplicateStationName,
            CheckedStationManager.Error.DuplicateStationLocation
          )
        )

    "existing station is removed" should:
      "no longer be present" in:
        StationManager.createCheckedStationManager().addStation(station).flatMap(
          _.removeStation(station)
        ).map(_.stations) match
          case Right(stations) => stations should not contain station
          case Left(_)         => fail()

    "non-existing station is removed" should:
      "return error" in:
        StationManager.createCheckedStationManager().removeStation(station) shouldBe Left(
          Chain(CheckedStationManager.Error.StationNotFound)
        )
