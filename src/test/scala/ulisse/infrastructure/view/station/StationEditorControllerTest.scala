package ulisse.infrastructure.view.station

import org.mockito.Mockito.when
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import ulisse.applications.adapters.StationPortInputAdapter
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station
import ulisse.applications.ports.StationPorts
import ulisse.applications.useCases.StationManager
import ulisse.infrastructures.view.station.StationEditorController
import ulisse.infrastructures.view.station.StationEditorController.given
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.applications.station.StationMap
import ulisse.utils.Errors.BaseError

class StationEditorControllerTest extends AnyWordSpec with Matchers:

  private val outputPort    = mock[StationPorts.Output]
  private val stationName   = "New Station"
  private val x             = 1
  private val y             = 1
  private val numberOfTrack = "1"
  "StationEditorController" when:
    "onOkClick is invoked" should:
      "add a new station when inputs are valid and oldStation is None" in:
        val inputPort  = StationPortInputAdapter[Int, Coordinate[Int]](StationManager(outputPort))
        val controller = StationEditorController[Int, Coordinate[Int]](inputPort)
        val station    = Station(stationName, Coordinate(x, y), 1)

        controller.onOkClick(
          stationName,
          x.toString,
          y.toString,
          numberOfTrack,
          None
        ) shouldBe Right(StationMap(station))

        controller.findStationAt(Coordinate(x, y)) shouldBe Some(station)

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
