//package ulisse.infrastructure.view.station
//
////import applications.station.{Manger, StationEditorController}
//import org.mockito.Mockito.when
//import org.scalatest.wordspec.AnyWordSpec
//import org.scalatest.matchers.should.Matchers
//import ulisse.applications.adapters.StationPortInputAdapter
//import ulisse.applications.ports.StationPorts
//import ulisse.applications.station.StationMap
//import ulisse.applications.useCases.StationManager
//import ulisse.entities.Coordinates.{Coordinate, Grid}
//import ulisse.entities.station.Station
//import ulisse.infrastructures.view.station.{StationEditorController, StationEditorView}
////import entities.Location
////import entities.station.Station
//import org.scalatestplus.mockito.MockitoSugar.mock
////
//class StationEditorControllerTest extends AnyWordSpec with Matchers:
//
//  private val view = mock[StationPorts.Output]
//  private val inputPort = StationPortInputAdapter[Int, Grid](StationManager(view))
//  "StationEditorController" when:
//    "onOkClick is invoked" should:
//      "add a new station when inputs are valid and oldStation is None" in:
//        val controller = StationEditorController(inputPort)
//
//        val stationName   = "New Station"
//        val latitude      = "1"
//        val longitude     = "1"
//        val numberOfTrack = "1"
//
//        Coordinate.createGrid(1, 1).toOption match
//          case Some(coordinate: Grid) =>
//            val station = Station[Int, Grid](stationName, coordinate, 1)
//            val station2 = Station[Int, Grid](stationName, coordinate, 1)
//            station shouldBe station2
//            StationMap().addStation(station) match
//              case Right(stationMap) =>
////                when(inputPort.addStation(station)).thenReturn(Right(station))
////                when(inputPort.stationMap).thenReturn(stationMap)
////                when(inputPort.findStationAt(coordinate)).thenReturn(Some(station))
//                print(controller.onOkClick(
//                  stationName,
//                  latitude,
//                  longitude,
//                  numberOfTrack,
//                  None
//                ))
//                controller.findStationAt(coordinate) shouldEqual Some(station)
//                fail()
//              case _ => fail()
//          case _ => fail()
//        val station = Station(stationName, Coordinate(1, 1), 1)
//
//
//
////
////      "replace the station when inputs are valid and oldStation is Some(station)" in:
////        val model      = Manger()
////        val controller = StationEditorController(view, model)
////
////        val oldStationName          = "Old Station"
////        val oldStationLatitude      = "1.0"
////        val oldStationLongitude     = "1.0"
////        val oldStationNumberOfTrack = "1"
////
////        val oldStation = Station(
////          oldStationName,
////          Location(oldStationLatitude.toDouble, oldStationLongitude.toDouble),
////          oldStationNumberOfTrack.toInt
////        )
////        controller.onOkClick(
////          oldStationName,
////          oldStationLatitude,
////          oldStationLongitude,
////          oldStationNumberOfTrack,
////          None
////        )
////
////        val stationName   = "New Station"
////        val latitude      = "2.0"
////        val longitude     = "2.0"
////        val numberOfTrack = "2"
////
////        val station = Station(
////          stationName,
////          Location(latitude.toDouble, longitude.toDouble),
////          numberOfTrack.toInt
////        )
////        controller.onOkClick(
////          stationName,
////          latitude,
////          longitude,
////          numberOfTrack,
////          Some(oldStation)
////        )
////
////        controller.findStationAt(Location(
////          latitude.toDouble,
////          longitude.toDouble
////        )) shouldEqual Some(station)
////        controller.findStationAt(Location(
////          oldStationLatitude.toDouble,
////          oldStationLongitude.toDouble
////        )) shouldEqual None
////
////      "throw an IllegalArgumentException when inputs are invalid" in:
////        val model      = Manger()
////        val controller = StationEditorController(view, model)
////
////        val stationName   = "New Station"
////        val latitude      = "1.0"
////        val longitude     = "1.0"
////        val numberOfTrack = "1"
////
////        intercept[IllegalArgumentException]:
////          controller.onOkClick(stationName, "a", longitude, numberOfTrack, None)
////
////        intercept[IllegalArgumentException]:
////          controller.onOkClick(stationName, latitude, "a", numberOfTrack, None)
////
////        intercept[IllegalArgumentException]:
////          controller.onOkClick(stationName, latitude, longitude, "a", None)
