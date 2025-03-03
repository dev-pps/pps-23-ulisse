package ulisse.applications.managers

import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Utils.MatchersUtils.shouldBeBoolean
import ulisse.applications.ports.UtilityPorts
import ulisse.dsl.comparison.FieldsComparators.compareTo
import ulisse.entities.simulation.data.Engine.*
import ulisse.entities.simulation.data.Engine
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment

class SimulationManagerTest extends AnyWordSpec with Matchers:

  private val timeProvider  = mock[UtilityPorts.Output.TimeProviderPort]
  private val startTime     = 10L
  private val timeIncrement = 5L
  private def setupTimeProvider(): Unit =
    val timeIterator = LazyList.iterate(startTime)(_ + timeIncrement).iterator
    when(timeProvider.currentTimeMillis()).thenAnswer((_: InvocationOnMock) => timeIterator.next())

  private def repeatDoStep(simulationManager: SimulationManager, times: Int): SimulationManager =
    (1 to times).foldLeft(simulationManager)((manager, _) => manager.doStep())

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
      pausedManager.engineState compareTo manager.engineState ignoring EngineStateField.Running shouldBeBoolean true
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
      updatedManager.engineState compareTo manager.engineState ignoring EngineStateField.LastUpdate shouldBeBoolean true
      updatedManager.engineState compareTo manager.engineState considering EngineStateField.LastUpdate shouldBeBoolean false
      updatedManager.simulationData.step shouldBe 1

  "BatchSimulationManager" should:
    "update state on multiple steps" in:
      for step <- 2 to 100 do
        setupTimeProvider()
        val realUpdate     = step - 1
        val manager        = SimulationManager.emptyBatchManager(timeProvider).start()
        val updatedManager = repeatDoStep(manager, step)
        updatedManager.engineState compareTo manager.engineState ignoring (EngineStateField.LastUpdate, EngineStateField.LastDelta, EngineStateField.ElapsedCycleTime) shouldBeBoolean true
        updatedManager.engineState compareTo manager.engineState considering EngineStateField.LastUpdate shouldBeBoolean false
        updatedManager.engineState compareTo manager.engineState considering EngineStateField.LastDelta shouldBeBoolean false
        updatedManager.engineState compareTo manager.engineState considering EngineStateField.ElapsedCycleTime shouldBeBoolean false
        updatedManager.engineState.state.lastUpdate shouldBe Some(startTime + realUpdate * timeIncrement)
        updatedManager.engineState.state.lastDelta shouldBe timeIncrement
        updatedManager.engineState.state.elapsedCycleTime shouldBe realUpdate * timeIncrement
        updatedManager.simulationData.step shouldBe step
        updatedManager.simulationData.secondElapsed shouldBe realUpdate * timeIncrement

  "TimedSimulationManager" should:
    "update state on multiple steps" in:
      val cps           = 10
      val cycleTimeStep = SimulationManager.calculateCycleTimeStep(cps)
      for step <- 2 to 100 do
        setupTimeProvider()
        val manager        = SimulationManager.emptyTimedManager(timeProvider, cps).start()
        val updatedManager = repeatDoStep(manager, step)
        val realUpdate     = step - 1
        val expectedStep   = (updatedManager.simulationData.secondElapsed / cycleTimeStep).toInt
        updatedManager.engineState compareTo manager.engineState ignoring (EngineStateField.LastUpdate, EngineStateField.LastDelta, EngineStateField.ElapsedCycleTime) shouldBeBoolean true
        updatedManager.engineState compareTo manager.engineState considering EngineStateField.LastUpdate shouldBeBoolean false
        updatedManager.engineState compareTo manager.engineState considering EngineStateField.LastDelta shouldBeBoolean false
        updatedManager.engineState compareTo manager.engineState considering EngineStateField.ElapsedCycleTime shouldBeBoolean realUpdate % (cycleTimeStep / timeIncrement) == 0
        updatedManager.engineState.state.lastUpdate shouldBe Some(startTime + realUpdate * timeIncrement)
        updatedManager.engineState.state.lastDelta shouldBe timeIncrement
        updatedManager.engineState.state.elapsedCycleTime shouldBe realUpdate * timeIncrement - expectedStep * cycleTimeStep
        updatedManager.simulationData.step shouldBe expectedStep
        updatedManager.simulationData.secondElapsed shouldBe realUpdate * timeIncrement
