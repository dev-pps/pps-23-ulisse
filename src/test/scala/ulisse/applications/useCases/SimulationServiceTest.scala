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
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SimulationServiceTest extends AnyWordSpec with Matchers:

  private type S = Station[?]
  private val notificationService     = mock[SimulationPorts.Output]
  private val timeProviderService     = mock[UtilityPorts.Output.TimeProviderPort]
  private val initialState            = AppState[S](StationManager.createCheckedStationManager())
  private val eventQueue              = LinkedBlockingQueue[AppState[S] => AppState[S]]()
  private val initialSimulationState  = SimulationState(SimulationManager.empty(timeProviderService))
  private val simulationQueue         = LinkedBlockingQueue[SimulationState => SimulationState]()
  private val simulationService       = SimulationService(eventQueue, simulationQueue, notificationService)
  private def updateAppState()        = runAll(initialState, eventQueue)
  private def updateSimulationState() = runAll(initialSimulationState, simulationQueue)
  private def updateState() =
    updateAppState()
    updateSimulationState()

  "SimulationService" should:
    "start simulation" in:
      val startSimulationResult = simulationService.start()
      updateState()
      Await.result(startSimulationResult, Duration.Inf).running shouldBe true

    "stop simulation" in:
      val startSimulationResult = simulationService.start()
      val stopSimulationResult  = simulationService.stop()
      updateState()
      Await.result(startSimulationResult, Duration.Inf).running shouldBe true
      Await.result(stopSimulationResult, Duration.Inf).running shouldBe false

    "reset simulation" in:
      val startSimulationResult = simulationService.start()
      val stopSimulationResult  = simulationService.stop()
      val resetSimulationResult = simulationService.reset()
      updateState()
      Await.result(startSimulationResult, Duration.Inf).running shouldBe true
      Await.result(stopSimulationResult, Duration.Inf).running shouldBe false
      Await.result(resetSimulationResult, Duration.Inf).running shouldBe false
