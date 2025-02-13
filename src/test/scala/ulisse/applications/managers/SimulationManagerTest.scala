package ulisse.applications.managers

import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Utils.MatchersUtils.shouldBeBoolean
import ulisse.applications.ports.UtilityPorts
import ulisse.entities.simulation.Environments.SimulationEnvironment
import ulisse.entities.simulation.Simulations.EngineState
import ulisse.entities.simulation.Simulations.EngineState.{ComparisonBuilder, Field}
import EngineState.*

class SimulationManagerTest extends AnyWordSpec with Matchers:

  private val timeProvider = mock[UtilityPorts.Output.TimeProviderPort]
  private def setupTimeProvider(): Unit =
    val timeIterator = LazyList.iterate(0L)(_ + 1).iterator
    when(timeProvider.currentTimeMillis()).thenAnswer((_: InvocationOnMock) => timeIterator.next())

  "Mocked TimeProvider" should:
    "return a sequence of increasing time values" in:
      setupTimeProvider()
      timeProvider.currentTimeMillis() shouldBe 0
      timeProvider.currentTimeMillis() shouldBe 1
      timeProvider.currentTimeMillis() shouldBe 2

  "SimulationManager" should:
    "not be running when created" in:
      SimulationManager.emptyBatchManager(timeProvider).engineState.running shouldBe false

    "be running after starting simulation" in:
      SimulationManager.emptyBatchManager(timeProvider).start().engineState.running shouldBe true

    "not be running after stopping simulation" in:
      SimulationManager.emptyBatchManager(timeProvider).start().stop().engineState.running shouldBe false

    "be running after restarting simulation" in:
      SimulationManager.emptyBatchManager(timeProvider).start().stop().start().engineState.running shouldBe true

    "not be running after resetting simulation" in:
      SimulationManager.emptyBatchManager(timeProvider).start().reset().engineState.running shouldBe false
      SimulationManager.emptyBatchManager(timeProvider).start().stop().reset().engineState.running shouldBe false

    "preserve state on stop" in:
      val manager       = SimulationManager.emptyBatchManager(timeProvider).start().doStep()
      val pausedManager = manager.stop()
      pausedManager.engineState compareTo manager.engineState ignoring Field.Running shouldBeBoolean true
      pausedManager.simulationData shouldBe manager.simulationData

    "clear state on reset" in:
      val manager      = SimulationManager.emptyBatchManager(timeProvider)
      val resetManager = manager.start().doStep().reset()
      resetManager.engineState shouldBe manager.engineState
      resetManager.simulationData shouldBe manager.simulationData
