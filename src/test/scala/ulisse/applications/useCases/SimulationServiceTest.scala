package ulisse.applications.useCases

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.applications.{AppState, SimulationState}
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.applications.ports.{SimulationPorts, UtilityPorts}
import ulisse.entities.simulation.Simulations.EngineState
import ulisse.entities.station.Station

import java.util.concurrent.LinkedBlockingQueue
import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SimulationServiceTest extends AnyWordSpec with Matchers:

  private val notificationService     = mock[SimulationPorts.Output]
  private val timeProviderService     = mock[UtilityPorts.Output.TimeProviderPort]
  private val initialState            = AppState.default()
  private val eventQueue              = LinkedBlockingQueue[AppState => AppState]()
  private val initialSimulationState  = SimulationState(SimulationManager.emptyBatchManager(timeProviderService))
  private val simulationQueue         = LinkedBlockingQueue[SimulationState => SimulationState]()
  private val simulationService       = SimulationService(eventQueue, simulationQueue, notificationService)
  private def updateAppState()        = runAll(initialState, eventQueue)
  private def updateSimulationState() = runAll(initialSimulationState, simulationQueue)
  private def updateState() =
    updateAppState()
    updateSimulationState()

  @tailrec
  private def doSteps(n: Int, currentState: Option[SimulationState]): SimulationState =
    (n, currentState) match
      case (0, Some(finalState)) => finalState
      case (_, Some(state))      => doSteps(n - 1, runAll(state, simulationQueue).lastOption)
      case _                     => fail()

  "SimulationService" should:
    // TODO add tests for setup (auto-setup on create)

    "start simulation" in:
      val startSimulationResult = simulationService.start()
      updateState()
      Await.result(startSimulationResult, Duration.Inf).running shouldBe true
      eventQueue.clear()

    "when started start to enqueue step handlers" in:
      for i <- 0 until 10 do
        simulationService.start()
        simulationQueue.size() shouldBe 1
        doSteps(i, updateState().lastOption).simulationManager.simulationData.step shouldBe i
        simulationQueue.size() shouldBe 1
        eventQueue.clear()

    "stop simulation" in:
      simulationService.start()
      val stopSimulationResult = simulationService.stop()
      updateState()
      Await.result(stopSimulationResult, Duration.Inf).running shouldBe false
      eventQueue.clear()

    "restart simulation" in:
      simulationService.start()
      simulationService.stop()
      val restartSimulationResult = simulationService.start()
      updateState()
      Await.result(restartSimulationResult, Duration.Inf).running shouldBe true
      eventQueue.clear()

    "reset simulation" in:
      simulationService.start()
      val resetSimulationResult = simulationService.reset()
      updateState()
      Await.result(resetSimulationResult, Duration.Inf).running shouldBe false
      eventQueue.clear()
