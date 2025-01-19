package ulisse.applications.station

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.applications.station.StationMap
import ulisse.entities.Coordinates.*
import ulisse.entities.station.Station

class StationMapTest extends AnyWordSpec with Matchers:

  "A StationMap" when:
    "created" should:
      "be empty" in:
        StationMap[Int, Grid]().stations shouldBe empty

    "a new station is added" should:
      "contain the new station" in:
        Coordinate.createGrid(0, 0).flatMap(
          Station.createCheckedStation("StationA", _, 1)
        ).toOption match
          case Some(value) =>
            StationMap[Int, Grid]().addStation(value).map(_.stations) match
              case Right(stations) => stations should contain only value
              case Left(_)         => fail()
          case None => fail()

    "another station with same name is added" should:
      "not be added and returns error" in:
        val station1 =
          Coordinate.createGrid(0, 0).flatMap(Station.createCheckedStation("StationA", _, 1)).toOption
        val station2 =
          Coordinate.createGrid(1, 1).flatMap(Station.createCheckedStation("StationA", _, 1)).toOption
        (station1, station2) match
          case (Some(s1), Some(s2)) =>
            StationMap[Int, Grid]().addStation(s1).flatMap(
              _.addStation(s2)
            ) shouldBe Left(StationMap.Error.DuplicateStationName)
          case (_, _) => fail()

    "another station with same location is added" should:
      "not be added and returns error" in:
        val station1 =
          Coordinate.createGrid(0, 0).flatMap(Station.createCheckedStation("StationA", _, 1)).toOption
        val station2 =
          Coordinate.createGrid(0, 0).flatMap(Station.createCheckedStation("StationB", _, 1)).toOption
        (station1, station2) match
          case (Some(s1), Some(s2)) =>
            StationMap[Int, Grid]().addStation(s1).flatMap(
              _.addStation(s2)
            ) shouldBe Left(StationMap.Error.DuplicateStationLocation)
          case (_, _) => fail()

    "existing station is removed" should:
      "no longer be present" in:
        Coordinate.createGrid(0, 0).flatMap(
          Station.createCheckedStation("StationA", _, 1)
        ).toOption match
          case Some(value) =>
            StationMap[Int, Grid]().addStation(value).flatMap(
              _.removeStation(value)
            ).map(_.stations) match
              case Right(stations) => stations should not contain value
              case Left(_)         => fail()
          case None => fail()

    "non-existing station is removed" should:
      "return error" in:
        Coordinate.createGrid(0, 0).flatMap(
          Station.createCheckedStation("StationA", _, 1)
        ).toOption match
          case Some(value) =>
            StationMap[Int, Grid]().removeStation(value) shouldBe Left(
              StationMap.Error.StationNotFound
            )
          case None => fail()
