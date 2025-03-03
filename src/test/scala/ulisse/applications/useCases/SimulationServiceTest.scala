package ulisse.applications.useCases

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.applications.ports.{SimulationPorts, UtilityPorts}
import ulisse.applications.{AppState, EventQueue}
import ulisse.entities.simulation.data.Engine
import ulisse.entities.station.Station

import java.util.concurrent.LinkedBlockingQueue
import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SimulationServiceTest extends AnyWordSpec with Matchers:

  private val notificationService = mock[SimulationPorts.Output]
  private val initialState        = AppState()
  private val eventQueue          = EventQueue()
  private val simulationService   = SimulationService(eventQueue, notificationService)
  private def updateState()       = runAll(initialState, eventQueue.events)

  @tailrec
  private def doSteps(n: Int, currentState: Option[AppState]): AppState =
    (n, currentState) match
      case (0, Some(finalState)) => finalState
      case (_, Some(state))      => doSteps(n - 1, runAll(state, eventQueue.events).lastOption)
      case _                     => fail()

  "SimulationService" should:
    // TODO add tests for setup (auto-setup on create)

    "start simulation" in:
      val startSimulationResult = simulationService.start()
      updateState()
      Await.result(startSimulationResult, Duration.Inf).running shouldBe true

    "when started start to enqueue step handlers" in:
      eventQueue.events.clear()
      for i <- 0 until 10 do
        simulationService.start()
        eventQueue.events.size() shouldBe 1
        doSteps(i, updateState().lastOption).simulationManager.simulationData.step shouldBe i
        eventQueue.events.size() shouldBe 1
        eventQueue.events.clear()

    "stop simulation" in:
      simulationService.initSimulation()
      simulationService.start()
      val stopSimulationResult = simulationService.stop()
      updateState()
      Await.result(stopSimulationResult, Duration.Inf).running shouldBe false

    "when stopped the enqueued step handler doesn't have effects" in:
      eventQueue.events.clear()
      for i <- 1 until 10 do // starts from 1 because if not the first step handler remains in the queue
        simulationService.start()
        simulationService.stop()
        eventQueue.events.size() shouldBe 2
        doSteps(i, updateState().lastOption).simulationManager.simulationData.step shouldBe 0
        eventQueue.events.size() shouldBe 0

    "restart simulation" in:
      simulationService.start()
      simulationService.stop()
      val restartSimulationResult = simulationService.start()
      updateState()
      Await.result(restartSimulationResult, Duration.Inf).running shouldBe true

    "reset simulation" in:
      simulationService.start()
      val resetSimulationResult = simulationService.reset()
      updateState()
      Await.result(resetSimulationResult, Duration.Inf).running shouldBe false
