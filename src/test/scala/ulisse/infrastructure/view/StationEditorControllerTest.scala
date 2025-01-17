package ulisse.infrastructure.view

//import applications.station.{Manger, StationEditorController}
//import org.scalatest.wordspec.AnyWordSpec
//import org.scalatest.matchers.should.Matchers
//import entities.Location
//import entities.station.Station
//import infrastructures.ui.station.StationEditorView
//import org.scalatestplus.mockito.MockitoSugar.mock
//
//class StationEditorControllerTest extends AnyWordSpec with Matchers:
//
//  private val view = mock[StationEditorView]
//  "StationEditorController" when:
//    "onOkClick is invoked" should:
//      "add a new station when inputs are valid and oldStation is None" in:
//        val model      = Manger()
//        val controller = StationEditorController(view, model)
//
//        val stationName   = "New Station"
//        val latitude      = "1.0"
//        val longitude     = "1.0"
//        val numberOfTrack = "1"
//
//        val station = Station(stationName, Location(1.0, 1.0), 1)
//        controller.onOkClick(
//          stationName,
//          latitude,
//          longitude,
//          numberOfTrack,
//          None
//        )
//
//        controller.findStationAt(Location(1.0, 1.0)) shouldEqual Some(station)
//
//      "replace the station when inputs are valid and oldStation is Some(station)" in:
//        val model      = Manger()
//        val controller = StationEditorController(view, model)
//
//        val oldStationName          = "Old Station"
//        val oldStationLatitude      = "1.0"
//        val oldStationLongitude     = "1.0"
//        val oldStationNumberOfTrack = "1"
//
//        val oldStation = Station(
//          oldStationName,
//          Location(oldStationLatitude.toDouble, oldStationLongitude.toDouble),
//          oldStationNumberOfTrack.toInt
//        )
//        controller.onOkClick(
//          oldStationName,
//          oldStationLatitude,
//          oldStationLongitude,
//          oldStationNumberOfTrack,
//          None
//        )
//
//        val stationName   = "New Station"
//        val latitude      = "2.0"
//        val longitude     = "2.0"
//        val numberOfTrack = "2"
//
//        val station = Station(
//          stationName,
//          Location(latitude.toDouble, longitude.toDouble),
//          numberOfTrack.toInt
//        )
//        controller.onOkClick(
//          stationName,
//          latitude,
//          longitude,
//          numberOfTrack,
//          Some(oldStation)
//        )
//
//        controller.findStationAt(Location(
//          latitude.toDouble,
//          longitude.toDouble
//        )) shouldEqual Some(station)
//        controller.findStationAt(Location(
//          oldStationLatitude.toDouble,
//          oldStationLongitude.toDouble
//        )) shouldEqual None
//
//      "throw an IllegalArgumentException when inputs are invalid" in:
//        val model      = Manger()
//        val controller = StationEditorController(view, model)
//
//        val stationName   = "New Station"
//        val latitude      = "1.0"
//        val longitude     = "1.0"
//        val numberOfTrack = "1"
//
//        intercept[IllegalArgumentException]:
//          controller.onOkClick(stationName, "a", longitude, numberOfTrack, None)
//
//        intercept[IllegalArgumentException]:
//          controller.onOkClick(stationName, latitude, "a", numberOfTrack, None)
//
//        intercept[IllegalArgumentException]:
//          controller.onOkClick(stationName, latitude, longitude, "a", None)
