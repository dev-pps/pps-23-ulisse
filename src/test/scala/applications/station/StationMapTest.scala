package applications.station

import entities.Location
import entities.Location.Grid
import entities.station.Station
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class StationMapTest extends AnyWordSpec with Matchers:

  val stations: List[Station[Grid]] = (for
    station1 <-
      Location.createGrid(0, 0).flatMap(Station("StationA", _, 1)).toOption
    station2 <-
      Location.createGrid(1, 1).flatMap(Station("StationB", _, 1)).toOption
    station3 <-
      Location.createGrid(2, 2).flatMap(Station("StationC", _, 1)).toOption
  yield List(station1, station2, station3)).fold(List.empty)(identity)

  "Station list correct initialization" in:
    stations.size shouldBe 3

  "A StationMap" when:
    "created with unique station names and locations" should:
      "be created successfully" in:
        StationMap[Grid, List[Station[Grid]]](stations) shouldBe a[Right[_, _]]

    "created with duplicate station names" should:
      "not be created successfully" in:
        val newStations = Location.createGrid(3, 3).flatMap(Station(
          "StationA",
          _,
          1
        )).toOption.map(_ +: stations).fold(stations)(identity)
        newStations.size shouldBe stations.size + 1
        StationMap[Grid, List[Station[Grid]]](newStations) shouldBe a[Left[
          _,
          _
        ]]

    "created with duplicate station locations" should:
      "not be created successfully" in:
        val newStations = Location.createGrid(2, 2).flatMap(Station(
          "StationD",
          _,
          1
        )).toOption.map(_ +: stations).fold(stations)(identity)
        newStations.size shouldBe stations.size + 1
        StationMap[Grid, List[Station[Grid]]](newStations) shouldBe a[Left[
          _,
          _
        ]]
