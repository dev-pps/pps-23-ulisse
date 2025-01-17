package ulisse.applications.useCases

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.applications.ports.StationPort
import ulisse.applications.useCases.StationManager
import ulisse.entities.Location
import ulisse.entities.Location.Grid
import ulisse.entities.station.Station

class StationManagerTest extends AnyWordSpec with Matchers:

  private val mockUI = mock[StationPort.Outbound]

  "StationManager" should:
    "initialize with an empty station map" in:
      StationManager[Grid](mockUI).stationMap.stations shouldBe empty

    "add a valid station to the station map" in:
      val stationManager = StationManager[Grid](mockUI)
      Location.createGrid(1, 1).flatMap(
        Station("Station1", _, 1)
      ).toOption match
        case Some(value) => stationManager.addStation(value) match
            case Right(_) =>
              stationManager.stationMap.stations should contain only value
            case Left(_) => fail()
        case None => fail()

    "add invalid station to the station map" in:
      val stationManager = StationManager[Grid](mockUI)
      val location1 =
        Location.createGrid(1, 1).flatMap(Station("Station1", _, 1)).toOption
      val location2 =
        Location.createGrid(1, 1).flatMap(Station("Station2", _, 1)).toOption
      (location1, location2) match
        case (Some(s1), Some(s2)) =>
          stationManager.addStation(s1).flatMap(
            _.addStation(s2)
          ) match
            case Right(_) => fail()
            case Left(_) =>
              stationManager.stationMap.stations should contain only s1
        case (_, _) => fail()

    "remove a present station from the station map" in:
      val stationManager = StationManager[Grid](mockUI)
      Location.createGrid(1, 1).flatMap(
        Station("Station1", _, 1)
      ).toOption match
        case Some(value) =>
          stationManager.addStation(value) match
            case Right(_) => stationManager.removeStation(value) match
                case Right(_) =>
                  stationManager.stationMap.stations shouldBe empty
                case Left(_) => fail()
            case Left(value) => fail()
        case None => fail()

    "not remove an absent station from the station map" in:
      val stationManager = StationManager[Grid](mockUI)
      val station1 =
        Location.createGrid(1, 1).flatMap(Station("Station1", _, 1)).toOption
      val station2 =
        Location.createGrid(2, 2).flatMap(Station("Station2", _, 1)).toOption
      (station1, station2) match
        case (Some(s1), Some(s2)) =>
          stationManager.addStation(s1).flatMap(
            _.removeStation(s2)
          ) match
            case Right(_) => fail()
            case Left(_) =>
              stationManager.stationMap.stations should contain only s1
        case (_, _) => fail()
