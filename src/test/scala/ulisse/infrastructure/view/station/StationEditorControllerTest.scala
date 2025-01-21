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

  private type N = Int
  private type C = Coordinate[N]
  private val outputPort    = mock[StationPorts.Output]
  private val stationName   = "New Station"
  private val x             = 1
  private val y             = 1
  private val numberOfTrack = 1
  private val station       = Station(stationName, Coordinate(x, y), numberOfTrack)

  "StationEditorController" when:
    "onOkClick is invoked" should:
      "add a new station when inputs are valid and oldStation is None" in:
        val inputPort  = StationPortInputAdapter[Int, Coordinate[Int]](StationManager(outputPort))
        val controller = StationEditorController[Int, Coordinate[Int]](inputPort)
        val station    = Station(stationName, Coordinate(x, y), numberOfTrack)

        controller.onOkClick(
          stationName,
          x.toString,
          y.toString,
          numberOfTrack.toString,
          None
        ) shouldBe Right(StationMap(station))

        controller.findStationAt(Coordinate(x, y)) shouldBe Some(station)

      "replace the station when inputs are valid and oldStation is Some(station)" in:
        val inputPort  = StationPortInputAdapter[Int, Coordinate[Int]](StationManager(outputPort))
        val controller = StationEditorController[Int, Coordinate[Int]](inputPort)
        val oldStation = Station(stationName, Coordinate(x, y), numberOfTrack)
        val newStation = Station(stationName, Coordinate(x + 1, y + 1), numberOfTrack + 1)

        controller.onOkClick(
          stationName,
          x.toString,
          y.toString,
          numberOfTrack.toString,
          None
        ) shouldBe Right(StationMap(oldStation))

        controller.findStationAt(Coordinate(x, y)) shouldBe Some(station)

        controller.onOkClick(
          stationName,
          (x + 1).toString,
          (y + 1).toString,
          (numberOfTrack + 1).toString,
          Some(oldStation)
        ) shouldBe Right(StationMap(newStation))

        controller.findStationAt(Coordinate(x, y)) shouldBe None
        controller.findStationAt(Coordinate(x + 1, y + 1)) shouldBe Some(newStation)

      "returns error when input are not valid" in:
        val inputPort  = StationPortInputAdapter[Int, Coordinate[Int]](StationManager(outputPort))
        val controller = StationEditorController[Int, Coordinate[Int]](inputPort)

        controller.onOkClick(
          stationName,
          "a",
          y.toString,
          numberOfTrack.toString,
          None
        ) shouldBe Left(StationEditorController.Error.InvalidRowFormat)

        controller.onOkClick(
          stationName,
          x.toString,
          "a",
          numberOfTrack.toString,
          None
        ) shouldBe Left(StationEditorController.Error.InvalidColumnFormat)

        controller.onOkClick(
          stationName,
          x.toString,
          y.toString,
          "a",
          None
        ) shouldBe Left(StationEditorController.Error.InvalidNumberOfTrackFormat)
