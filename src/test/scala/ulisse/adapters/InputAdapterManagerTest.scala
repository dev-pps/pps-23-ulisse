package ulisse.adapters

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.adapters.input.TimetableViewAdapters.TimetableViewAdapter
import ulisse.adapters.input.{
  RouteAdapter,
  SimulationInfoAdapter,
  SimulationPageAdapter,
  StationEditorAdapter,
  TrainViewAdapter
}

class InputAdapterManagerTest extends AnyFlatSpec with Matchers:

  "create input adapter manager" should "create input adapter manager" in:
    val station        = mock[StationEditorAdapter]
    val route          = mock[RouteAdapter]
    val train          = mock[TrainViewAdapter]
    val timetable      = mock[TimetableViewAdapter]
    val simulationPage = mock[SimulationPageAdapter]
    val simulationInfo = mock[SimulationInfoAdapter]
    val inputAdapterManager =
      InputAdapterManager.create(station, route, train, timetable, simulationPage, simulationInfo)

    inputAdapterManager.station mustBe station
    inputAdapterManager.route mustBe route
    inputAdapterManager.train mustBe train
    inputAdapterManager.timetable mustBe timetable
    inputAdapterManager.simulationPage mustBe simulationPage
    inputAdapterManager.simulationInfo mustBe simulationInfo
