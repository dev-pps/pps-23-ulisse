package ulisse.applications.useCases

import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.Utils.MatchersUtils.shouldBeBoolean
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.ports.SimulationPorts
import ulisse.applications.{AppState, EventQueue}
import ulisse.dsl.comparison.FieldsComparators.compareTo
import ulisse.entities.simulation.data.Engine.EngineField.Running
import ulisse.entities.simulation.data.SimulationData.SimulationDataField.SimulationEnvironment
import ulisse.entities.simulation.data.{Engine, EngineConfiguration, SimulationData}
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationDataTest.simpleConfigurationData
import ulisse.entities.simulation.environments.railwayEnvironment.{ConfigurationData, RailwayEnvironment}

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SimulationServiceTest extends AnyWordSpec with Matchers with BeforeAndAfterEach:
  private val notificationService = mock[SimulationPorts.Output]

  private val stationManager: StationManager = mock[StationManager]
  when(stationManager.stations).thenReturn(simpleConfigurationData.stations)
  private val routeManager: RouteManager = mock[RouteManager]
  when(routeManager.routes).thenReturn(simpleConfigurationData.routes)
  private val trainManager: TrainManager = mock[TrainManager]
  when(trainManager.trains).thenReturn(simpleConfigurationData.trains)
  private val timetableManager: TimetableManager = mock[TimetableManager]
  when(timetableManager.tables).thenReturn(simpleConfigurationData.timetablesByTrain.values.flatten.toSeq)

  private val initialState = AppState().updateStationManager(_ => stationManager)
    .updateRoute(_ => routeManager)
    .updateTrain((_, _) => trainManager)
    .updateTimetable(_ => timetableManager)

  private val eventQueue        = EventQueue()
  private val simulationService = SimulationService(eventQueue, notificationService)
  private def updateState() =
    runAll(initialState, eventQueue.events)

  @tailrec
  private def doSteps(n: Int, currentState: Option[AppState]): AppState =
    (n, currentState) match
      case (0, Some(finalState)) => finalState
      case (_, Some(state))      => doSteps(n - 1, runAll(state, eventQueue.events).lastOption)
      case _                     => fail()

  private def evaluateSettings(engine: Engine, simulationData: SimulationData): Unit =
    engine shouldBe Engine.emptyWithConfiguration(EngineConfiguration.defaultBatch())
    simulationData compareTo SimulationData.empty() ignoring SimulationEnvironment shouldBeBoolean true
    simulationData.simulationEnvironment shouldBe RailwayEnvironment.auto(simpleConfigurationData)

  override def beforeEach(): Unit =
    eventQueue.events.clear()

  "SimulationService" should:
    "init simulation" in:
      val initSimulationResult = simulationService.initSimulation()
      updateState()
      evaluateSettings.tupled(Await.result(initSimulationResult, Duration.Inf))

    "setup engine" in:
      val ec                = EngineConfiguration(10, Some(10))
      val setupEngineResult = simulationService.setupEngine.tupled(ec.stepSize, ec.cyclesPerSecond)
      updateState()
      Await.result(setupEngineResult, Duration.Inf) shouldBe Some(Engine.emptyWithConfiguration(ec))

    "fail to setup engine with negative step size" in:
      val ec                = (-1, Some(10))
      val setupEngineResult = simulationService.setupEngine.tupled(ec)
      updateState()
      Await.result(setupEngineResult, Duration.Inf) shouldBe None

    "fail to setup engine with non-positive cycles per second" in:
      val ec                = (10, Some(0))
      val setupEngineResult = simulationService.setupEngine.tupled(ec)
      updateState()
      Await.result(setupEngineResult, Duration.Inf) shouldBe None

    "maintain engine setup on init" in:
      val ec                   = EngineConfiguration(10, Some(10))
      val setupEngineResult    = simulationService.setupEngine(ec.stepSize, ec.cyclesPerSecond)
      val initSimulationResult = simulationService.initSimulation()
      updateState()
      Await.result(setupEngineResult, Duration.Inf) shouldBe Some(Engine.emptyWithConfiguration(ec))
      val (engine, _) = Await.result(initSimulationResult, Duration.Inf)
      engine shouldBe Engine.emptyWithConfiguration(ec)

    "start simulation" in:
      val startSimulationResult = simulationService.start()
      updateState()
      Await.result(startSimulationResult, Duration.Inf).running shouldBe true

    "when started start to enqueue step handlers" in:
      for i <- 0 until 5 do
        simulationService.initSimulation()
        simulationService.start()
        eventQueue.events.size() shouldBe 2
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
      for i <- 1 until 5 do // starts from 1 because if not the first step handler remains in the queue
        simulationService.initSimulation()
        simulationService.start()
        simulationService.stop()
        eventQueue.events.size() shouldBe 3
        doSteps(i, updateState().lastOption).simulationManager.simulationData.step shouldBe 0
        eventQueue.events.size() shouldBe 0

    "restart simulation" in:
      simulationService.start()
      simulationService.stop()
      val restartSimulationResult = simulationService.start()
      updateState()
      val engine = Await.result(restartSimulationResult, Duration.Inf)
      engine.running shouldBe true
      engine compareTo Engine.emptyWithConfiguration(
        EngineConfiguration.defaultBatch()
      ) ignoring Running shouldBeBoolean true

    "reset simulation" in:
      simulationService.initSimulation()
      simulationService.start()
      val resetSimulationResult = simulationService.reset()
      updateState()
      evaluateSettings.tupled(Await.result(resetSimulationResult, Duration.Inf))
