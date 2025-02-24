package ulisse.adapters.input

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.applications.ports.SimulationPorts
import ulisse.applications.useCases.SimulationService
import ulisse.entities.simulation.Simulations.EngineState

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class SimulationPageAdapterTest extends AnyWordSpec with Matchers:

  private val mockedPort            = mock[SimulationPorts.Input]
  private val simulationPageAdapter = SimulationPageAdapter(mockedPort)
  private val defaultEngineState    = EngineState(false, None, None, 0, 0)

  "SimulationPageAdapter" should:
    "start simulation" in:
      when(mockedPort.start()).thenReturn(Future.successful(defaultEngineState))
      Await.result(simulationPageAdapter.start(), Duration.Inf) shouldBe defaultEngineState

    "stop simulation" in:
      when(mockedPort.stop()).thenReturn(Future.successful(defaultEngineState))
      Await.result(simulationPageAdapter.stop(), Duration.Inf) shouldBe defaultEngineState

    "reset simulation" in:
      when(mockedPort.reset()).thenReturn(Future.successful(defaultEngineState))
      Await.result(simulationPageAdapter.reset(), Duration.Inf) shouldBe defaultEngineState
