//package ulisse.applications.useCases
//
//import org.scalatest.matchers.should.Matchers
//import org.scalatest.wordspec.AnyWordSpec
//import org.scalatestplus.mockito.MockitoSugar.mock
//import ulisse.applications.ports.StationPorts
//import ulisse.applications.station.StationMap
//import ulisse.applications.useCases.StationManager
//import ulisse.entities.Coordinates.*
//import ulisse.entities.station.Station
//
//class StationManagerTest extends AnyWordSpec with Matchers:
//
//  private val mockUI  = mock[StationPorts.Output]
//  private val station = Station("StationA", Coordinate(0, 0), 1)
//
//  "StationManager" should:
//    "initialize with an empty station map" in:
//      StationManager[Int, Coordinate[Int], Station[Int, Coordinate[Int]]](mockUI).stationMap.stations shouldBe empty
//
//    "add a valid station to the station map" in:
//      val stationManager = StationManager[Int, Coordinate[Int], Station[Int, Coordinate[Int]]](mockUI)
//      stationManager.addStation(station) match
//        case Right(_) =>
//          stationManager.stationMap.stations should contain only station
//        case Left(_) => fail()
//
//    "add invalid station to the station map" in:
//      val stationManager = StationManager[Int, Coordinate[Int], Station[Int, Coordinate[Int]]](mockUI)
//      stationManager.addStation(station)
//      stationManager.addStation(station) match
//        case Right(_) => fail()
//        case Left(_)  => stationManager.stationMap.stations should contain only station
//
//    "remove a present station from the station map" in:
//      val stationManager = StationManager[Int, Coordinate[Int], Station[Int, Coordinate[Int]]](mockUI)
//      stationManager.addStation(station)
//      stationManager.removeStation(station)
//      stationManager.stationMap.stations shouldBe empty
//
//    "not remove an absent station from the station map" in:
//      val stationManager = StationManager[Int, Coordinate[Int], Station[Int, Coordinate[Int]]](mockUI)
//      val otherStation   = Station("StationB", Coordinate(1, 1), 1)
//      stationManager.addStation(station)
//      stationManager.removeStation(otherStation) match
//        case Right(_) => fail()
//        case Left(_)  => stationManager.stationMap.stations should contain only station
