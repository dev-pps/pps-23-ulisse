package ulisse.applications

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.applications.ports.*

class InputPortManagerTest extends AnyFlatSpec with Matchers:

  "create input port manager" should "create input port manager" in:
    val queue            = mock[EventQueue]
    val station          = mock[StationPorts.Input]
    val route            = mock[RoutePorts.Input]
    val train            = mock[TrainPorts.Input]
    val timetable        = mock[TimetablePorts.Input]
    val simulation       = mock[SimulationPorts.Input]
    val simulationInfo   = mock[SimulationInfoPorts.Input]
    val inputPortManager = InputPortManager.create(queue, station, route, train, timetable, simulation, simulationInfo)

    inputPortManager.station mustBe station
    inputPortManager.route mustBe route
    inputPortManager.train mustBe train
    inputPortManager.timetable mustBe timetable
    inputPortManager.simulation mustBe simulation
    inputPortManager.simulationInfo mustBe simulationInfo
