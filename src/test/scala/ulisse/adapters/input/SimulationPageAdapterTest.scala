package ulisse.adapters.input

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.applications.useCases.SimulationService
import ulisse.entities.simulation.Simulations.EngineState

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class SimulationPageAdapterTest extends AnyWordSpec with Matchers:

  private val mockedService         = mock[SimulationService]
  private val simulationPageAdapter = SimulationPageAdapter(mockedService)
  private val defaultEngineState    = EngineState(false, None, None, 0, 0)

  "SimulationPageAdapter" should:
    "start simulation" in:
      when(mockedService.start()).thenReturn(Future.successful(defaultEngineState))
      Await.result(simulationPageAdapter.start(), Duration.Inf) shouldBe defaultEngineState

    "stop simulation" in:
      when(mockedService.stop()).thenReturn(Future.successful(defaultEngineState))
      Await.result(simulationPageAdapter.stop(), Duration.Inf) shouldBe defaultEngineState

    "reset simulation" in:
      when(mockedService.reset()).thenReturn(Future.successful(defaultEngineState))
      Await.result(simulationPageAdapter.reset(), Duration.Inf) shouldBe defaultEngineState
