package ulisse.applications.station

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.applications.station.StationMap
import ulisse.entities.Coordinates.*
import ulisse.entities.station.Station

class StationMapTest extends AnyWordSpec with Matchers:

  private val station = Station("StationA", Coordinate(0, 0), 1)

  "A StationMap" when:
    "created" should:
      "be empty" in:
        StationMap[Int, Grid]().stations shouldBe empty

    "a new station is added" should:
      "contain the new station" in:
        StationMap().addStation(station).map(_.stations) match
          case Right(stations) =>
            stations should contain only station
          case Left(_) => fail()

    "another station with same name is added" should:
      "not be added and returns error" in:
        val otherStation = Station("StationA", Coordinate(1, 1), 1)
        StationMap().addStation(station).flatMap(
          _.addStation(otherStation)
        ) shouldBe Left(StationMap.Error.DuplicateStationName)

    "another station with same location is added" should:
      "not be added and returns error" in:
        val otherStation = Station("StationB", Coordinate(0, 0), 1)
        StationMap().addStation(station).flatMap(
          _.addStation(otherStation)
        ) shouldBe Left(StationMap.Error.DuplicateStationLocation)

    "existing station is removed" should:
      "no longer be present" in:
        StationMap().addStation(station).flatMap(
          _.removeStation(station)
        ).map(_.stations) match
          case Right(stations) => stations should not contain station
          case Left(_)         => fail()

    "non-existing station is removed" should:
      "return error" in:
        StationMap().removeStation(station) shouldBe Left(
          StationMap.Error.StationNotFound
        )
