package ulisse.applications.managers

import org.mockito.Mockito.{spy, when}
import org.mockito.invocation.InvocationOnMock
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Utils.MatchersUtils.shouldBeBoolean
import ulisse.applications.ports.UtilityPorts
import ulisse.dsl.comparison.FieldsComparators.compareTo
import ulisse.entities.simulation.data.Engine.*
import ulisse.entities.simulation.data.SimulationData.SimulationDataField.SimulationEnvironment
import ulisse.entities.simulation.data.{Engine, EngineConfiguration, SimulationData}
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationDataTest.simpleConfigurationData
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.entities.timetable.DynamicTimetableTest.dynamicTimetable1

class SimulationManagerTest extends AnyWordSpec with Matchers:
  private val sdtt = spy(dynamicTimetable1)
  when(sdtt.completed).thenReturn(false)
  private val mockedEnv = mock[RailwayEnvironment]
  when(mockedEnv.doStep(1)).thenReturn(mockedEnv)
  when(mockedEnv.timetables).thenReturn(Seq(sdtt))
  private val timeProvider  = mock[UtilityPorts.Output.TimeProviderPort]
  private val startTime     = 10L
  private val timeIncrement = 5L
  private val sm            = SimulationManager.defaultBatchManager(timeProvider).setupEnvironment(mockedEnv)
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

  def checkBaseConfiguration(sm: SimulationManager, ec: EngineConfiguration): Unit =
    sm.engine shouldBe Engine.emptyWithConfiguration(ec)
    sm.simulationData shouldBe SimulationData.empty()
  private val ec = EngineConfiguration(10, Some(10))

  "SimulationManager" when:
    "created" should:
      "have base configuration" in:
        checkBaseConfiguration(SimulationManager(None, timeProvider, ec), ec)

    "configured" should:
      "have base configuration" in:
        checkBaseConfiguration(SimulationManager(None, timeProvider, ec), ec)

    "defaultTimed" should:
      "have default timed configuration" in:
        checkBaseConfiguration(SimulationManager.defaultTimedManager(timeProvider), EngineConfiguration.defaultTimed())

    "defaultBatch" should:
      "have default batch configuration" in:
        checkBaseConfiguration(SimulationManager.defaultBatchManager(timeProvider), EngineConfiguration.defaultBatch())

    "calculated cycle time step" should:
      "be 1000 ms for 1 cps" in:
        SimulationManager.calculateCycleTimeStep(1) shouldBe 1000.0
      "be 500 ms for 2 cps" in:
        SimulationManager.calculateCycleTimeStep(2) shouldBe 500.0
      "be 100 ms for 10 cps" in:
        SimulationManager.calculateCycleTimeStep(10) shouldBe 100.0

    "setup engine" should:
      "update configuration" in:
        val manager = SimulationManager.defaultBatchManager(timeProvider)
        manager.setupEngine(10, Some(10)) match
          case Some(newManager) =>
            newManager.engine.configuration shouldBe EngineConfiguration(10, Some(10))
            newManager.simulationData shouldBe manager.simulationData
          case _ => fail()

    "setup environment" should:
      "update environment" in:
        val manager     = SimulationManager.defaultBatchManager(timeProvider)
        val environment = RailwayEnvironment.auto(simpleConfigurationData)
        val newManager  = manager.setupEnvironment(environment)
        newManager.engine shouldBe manager.engine
        newManager.simulationData compareTo manager.simulationData ignoring SimulationEnvironment shouldBeBoolean true
        newManager.simulationData.simulationEnvironment shouldBe environment

    "be running after starting simulation" in:
      sm.start().engine.running shouldBe true

    "not be running after stopping simulation" in:
      sm.start().stop().engine.running shouldBe false

    "be running after restarting simulation" in:
      sm.start().stop().start().engine.running shouldBe true

    "not be running after resetting simulation" in:
      sm.start().reset().engine.running shouldBe false
      sm.start().stop().reset().engine.running shouldBe false

    "preserve state on stop" in:
      setupTimeProvider()
      val manager       = SimulationManager.defaultBatchManager(timeProvider).start().doStep()
      val pausedManager = manager.stop()
      pausedManager.engine compareTo manager.engine ignoring EngineStateField.Running shouldBeBoolean true
      pausedManager.simulationData shouldBe manager.simulationData

    "clear state on reset" in:
      setupTimeProvider()
      val manager      = SimulationManager.defaultBatchManager(timeProvider)
      val resetManager = manager.start().doStep().reset()
      resetManager.engine shouldBe manager.engine
      resetManager.simulationData shouldBe manager.simulationData

    "update state on step" in:
      setupTimeProvider()
      val manager        = SimulationManager.defaultBatchManager(timeProvider).setupEnvironment(mockedEnv).start()
      val updatedManager = manager.doStep()
      updatedManager.engine compareTo manager.engine ignoring EngineStateField.LastUpdate shouldBeBoolean true
      updatedManager.engine compareTo manager.engine considering EngineStateField.LastUpdate shouldBeBoolean false
      updatedManager.simulationData.step shouldBe 1

  "BatchSimulationManager" should:
    "update state on multiple steps" in:
      for step <- 2 to 100 do
        setupTimeProvider()
        val realUpdate     = step - 1
        val manager        = SimulationManager.defaultBatchManager(timeProvider).setupEnvironment(mockedEnv).start()
        val updatedManager = repeatDoStep(manager, step)
        updatedManager.engine compareTo manager.engine ignoring (EngineStateField.LastUpdate, EngineStateField.LastDelta, EngineStateField.ElapsedCycleTime) shouldBeBoolean true
        updatedManager.engine compareTo manager.engine considering EngineStateField.LastUpdate shouldBeBoolean false
        updatedManager.engine compareTo manager.engine considering EngineStateField.LastDelta shouldBeBoolean false
        updatedManager.engine compareTo manager.engine considering EngineStateField.ElapsedCycleTime shouldBeBoolean false
        updatedManager.engine.state.lastUpdate shouldBe Some(startTime + realUpdate * timeIncrement)
        updatedManager.engine.state.lastDelta shouldBe timeIncrement
        updatedManager.engine.state.elapsedCycleTime shouldBe realUpdate * timeIncrement
        updatedManager.simulationData.step shouldBe step
        updatedManager.simulationData.secondElapsed shouldBe realUpdate * timeIncrement

  "TimedSimulationManager" should:
    "update state on multiple steps" in:
      val cps           = 10
      val cycleTimeStep = SimulationManager.calculateCycleTimeStep(cps)
      for step <- 2 to 100 do
        setupTimeProvider()
        val manager = SimulationManager.configuredManager(
          timeProvider,
          EngineConfiguration.withCps(cps)
        ).setupEnvironment(mockedEnv).start()
        val updatedManager = repeatDoStep(manager, step)
        val realUpdate     = step - 1
        val expectedStep   = (updatedManager.simulationData.secondElapsed / cycleTimeStep).toInt
        updatedManager.engine compareTo manager.engine ignoring (EngineStateField.LastUpdate, EngineStateField.LastDelta, EngineStateField.ElapsedCycleTime) shouldBeBoolean true
        updatedManager.engine compareTo manager.engine considering EngineStateField.LastUpdate shouldBeBoolean false
        updatedManager.engine compareTo manager.engine considering EngineStateField.LastDelta shouldBeBoolean false
        updatedManager.engine compareTo manager.engine considering EngineStateField.ElapsedCycleTime shouldBeBoolean realUpdate % (cycleTimeStep / timeIncrement) == 0
        updatedManager.engine.state.lastUpdate shouldBe Some(startTime + realUpdate * timeIncrement)
        updatedManager.engine.state.lastDelta shouldBe timeIncrement
        updatedManager.engine.state.elapsedCycleTime shouldBe realUpdate * timeIncrement - expectedStep * cycleTimeStep
        updatedManager.simulationData.step shouldBe expectedStep
        updatedManager.simulationData.secondElapsed shouldBe realUpdate * timeIncrement
