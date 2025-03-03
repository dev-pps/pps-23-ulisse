package ulisse.adapters.input

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.applications.ports.SimulationPorts
import ulisse.applications.useCases.SimulationService
import ulisse.entities.simulation.data.{EngineState, SimulationData}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class SimulationPageAdapterTest extends AnyWordSpec with Matchers:

  private val mockedPort            = mock[SimulationPorts.Input]
  private val simulationPageAdapter = SimulationPageAdapter(mockedPort)
  private val defaultEngineState    = EngineState.empty()
  private val defaultSimulationData = SimulationData.empty()

  "SimulationPageAdapter" should:
    "setup simulation" in:
      when(mockedPort.initSimulation()).thenReturn(Future.successful((defaultEngineState, defaultSimulationData)))
      Await.result(
        simulationPageAdapter.initSimulation(),
        Duration.Inf
      ) shouldBe (defaultEngineState, defaultSimulationData)

    "setup engine" in:
      val stepSize           = 1
      val cyclesPerSecond    = Some(1)
      val updatedEngineState = defaultEngineState.copy(cyclesPerSecond = cyclesPerSecond, stepSize = stepSize)
      when(mockedPort.setupEngine(stepSize, cyclesPerSecond)).thenReturn(Future.successful(updatedEngineState))
      Await.result(
        simulationPageAdapter.setupEngine(stepSize, cyclesPerSecond),
        Duration.Inf
      ) shouldBe updatedEngineState

    "not setup engine" in:
      val stepSize        = -1
      val cyclesPerSecond = None
      when(mockedPort.setupEngine(stepSize, cyclesPerSecond)).thenReturn(Future.successful(None))
      Await.result(
        simulationPageAdapter.setupEngine(stepSize, cyclesPerSecond),
        Duration.Inf
      ) shouldBe None

    "start simulation" in:
      when(mockedPort.start()).thenReturn(Future.successful(defaultEngineState))
      Await.result(simulationPageAdapter.start(), Duration.Inf) shouldBe defaultEngineState

    "stop simulation" in:
      when(mockedPort.stop()).thenReturn(Future.successful(defaultEngineState))
      Await.result(simulationPageAdapter.stop(), Duration.Inf) shouldBe defaultEngineState

    "reset simulation" in:
      when(mockedPort.reset()).thenReturn(Future.successful(defaultEngineState))
      Await.result(simulationPageAdapter.reset(), Duration.Inf) shouldBe defaultEngineState
