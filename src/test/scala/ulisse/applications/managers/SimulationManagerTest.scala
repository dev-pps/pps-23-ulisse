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
import ulisse.entities.simulation.Simulations.EngineState.Field.LastDelta

class SimulationManagerTest extends AnyWordSpec with Matchers:

  private val timeProvider  = mock[UtilityPorts.Output.TimeProviderPort]
  private val startTime     = 10L
  private val timeIncrement = 5L
  private def setupTimeProvider(): Unit =
    val timeIterator = LazyList.iterate(startTime)(_ + timeIncrement).iterator
    when(timeProvider.currentTimeMillis()).thenAnswer((_: InvocationOnMock) => timeIterator.next())

  "Mocked TimeProvider" should:
    "return a sequence of increasing time values" in:
      setupTimeProvider()
      timeProvider.currentTimeMillis() shouldBe startTime
      timeProvider.currentTimeMillis() shouldBe startTime + timeIncrement
      timeProvider.currentTimeMillis() shouldBe startTime + 2 * timeIncrement

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
      setupTimeProvider()
      val manager       = SimulationManager.emptyBatchManager(timeProvider).start().doStep()
      val pausedManager = manager.stop()
      pausedManager.engineState compareTo manager.engineState ignoring Field.Running shouldBeBoolean true
      pausedManager.simulationData shouldBe manager.simulationData

    "clear state on reset" in:
      setupTimeProvider()
      val manager      = SimulationManager.emptyBatchManager(timeProvider)
      val resetManager = manager.start().doStep().reset()
      resetManager.engineState shouldBe manager.engineState
      resetManager.simulationData shouldBe manager.simulationData

    "update state on step" in:
      setupTimeProvider()
      val manager        = SimulationManager.emptyBatchManager(timeProvider).start()
      val updatedManager = manager.doStep()
      updatedManager.engineState compareTo manager.engineState ignoring Field.LastUpdate shouldBeBoolean true
      updatedManager.engineState compareTo manager.engineState considering Field.LastUpdate shouldBeBoolean false
      updatedManager.simulationData.step shouldBe 1

  "BatchSimulationManager" should:
    "update state on multiple steps" in:
      setupTimeProvider()
      val manager        = SimulationManager.emptyBatchManager(timeProvider).start()
      val updatedManager = manager.doStep().doStep().doStep()
      updatedManager.engineState compareTo manager.engineState ignoring (Field.LastUpdate, Field.LastDelta, Field.ElapsedCycleTime) shouldBeBoolean true
      updatedManager.engineState compareTo manager.engineState considering Field.LastUpdate shouldBeBoolean false
      updatedManager.engineState compareTo manager.engineState considering Field.LastDelta shouldBeBoolean false
      updatedManager.engineState compareTo manager.engineState considering Field.ElapsedCycleTime shouldBeBoolean false
      updatedManager.engineState.lastUpdate shouldBe Some(startTime + 2 * timeIncrement)
      updatedManager.engineState.lastDelta shouldBe timeIncrement
      updatedManager.engineState.elapsedCycleTime shouldBe 2 * timeIncrement
      updatedManager.simulationData.step shouldBe 3
      updatedManager.simulationData.secondElapsed shouldBe 2 * timeIncrement

  "TimedSimulationManager" should:
    "update state on multiple steps" in:
      setupTimeProvider()
      val manager        = SimulationManager.emptyTimedManager(timeProvider, 10).start()
      val updatedManager = manager.doStep().doStep().doStep()
      updatedManager.engineState compareTo manager.engineState ignoring (Field.LastUpdate, Field.LastDelta, Field.ElapsedCycleTime) shouldBeBoolean true
      updatedManager.engineState compareTo manager.engineState considering Field.LastUpdate shouldBeBoolean false
      updatedManager.engineState compareTo manager.engineState considering Field.LastDelta shouldBeBoolean false
      updatedManager.engineState compareTo manager.engineState considering Field.ElapsedCycleTime shouldBeBoolean false
      updatedManager.engineState.lastUpdate shouldBe Some(startTime + 2 * timeIncrement)
      updatedManager.engineState.lastDelta shouldBe timeIncrement
      updatedManager.engineState.elapsedCycleTime shouldBe 2 * timeIncrement
      updatedManager.simulationData.step shouldBe 0
      updatedManager.simulationData.secondElapsed shouldBe 2 * timeIncrement
