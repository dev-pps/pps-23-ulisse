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
import ulisse.applications.ports.{SimulationInfoPorts, SimulationPorts}
import ulisse.applications.{EventQueue, InputPortManager}

class InputAdapterManagerTest extends AnyFlatSpec with Matchers:

  "create input adapter manager" should "create input adapter manager" in:
    val eventQueue       = mock[EventQueue]
    val simulation       = mock[SimulationPorts.Input]
    val simulationInfo   = mock[SimulationInfoPorts.Input]
    val inputPortManager = InputPortManager(eventQueue, simulation, simulationInfo)

    val simulationPageAdp   = mock[SimulationPageAdapter]
    val simulationInfoAdp   = mock[SimulationInfoAdapter]
    val inputAdapterManager = InputAdapterManager(inputPortManager, simulationPageAdp, simulationInfoAdp)

    inputAdapterManager.station mustBe a[StationEditorAdapter]
    inputAdapterManager.route mustBe a[RouteAdapter]
    inputAdapterManager.train mustBe a[TrainViewAdapter]
    inputAdapterManager.timetable mustBe a[TimetableViewAdapter]
    inputAdapterManager.simulationPage mustBe simulationPageAdp
    inputAdapterManager.simulationInfo mustBe simulationInfoAdp
