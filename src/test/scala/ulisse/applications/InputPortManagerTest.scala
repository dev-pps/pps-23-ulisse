package ulisse.applications

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.applications.ports.*
import ulisse.applications.useCases.{RouteService, StationService, TimetableService, TrainService}

class InputPortManagerTest extends AnyFlatSpec with Matchers:

  "create input port manager" should "create input port manager" in:
    val eventQueue       = mock[EventQueue]
    val simulation       = mock[SimulationPorts.Input]
    val simulationInfo   = mock[SimulationInfoPorts.Input]
    val inputPortManager = InputPortManager(eventQueue, simulation, simulationInfo)

    inputPortManager.station mustBe StationService(eventQueue)
    inputPortManager.route mustBe RouteService(eventQueue)
    inputPortManager.train mustBe TrainService(eventQueue)
    inputPortManager.timetable mustBe TimetableService(eventQueue)
    inputPortManager.simulation mustBe simulation
    inputPortManager.simulationInfo mustBe simulationInfo
