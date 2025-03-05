package ulisse.applications.useCases

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.applications.ports.{SimulationPorts, UtilityPorts}
import ulisse.applications.{AppState, EventQueue}
import ulisse.dsl.comparison.FieldsComparators.compareTo
import ulisse.entities.route.RouteEnvironmentElementTest.routeAB_EE
import ulisse.entities.simulation.data.SimulationData.SimulationDataField.SimulationEnvironment
import ulisse.entities.simulation.data.{Engine, EngineConfiguration, SimulationData}
import ulisse.entities.simulation.environments.railwayEnvironment.{ConfigurationData, RailwayEnvironment}
import ulisse.entities.station.Station
import ulisse.entities.station.StationEnvironmentElementTest.{stationA_EE, stationB_EE}
import ulisse.entities.station.StationTest.stationA
import ulisse.entities.timetable.DynamicTimetableTest.dynamicTimetable1
import ulisse.entities.train.TrainAgentTest.trainAgent3905
import ulisse.entities.train.Trains.TrainTechnology
import ulisse.Utils.MatchersUtils.shouldBeBoolean
import ulisse.entities.simulation.data.Engine.EngineStateField.Running
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationDataTest.simpleConfigurationData
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable

import java.util.concurrent.LinkedBlockingQueue
import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SimulationServiceTest extends AnyWordSpec with Matchers:

  private val notificationService = mock[SimulationPorts.Output]

  private val stationManager: StationManager = mock[StationManager]
  when(stationManager.stations).thenReturn(simpleConfigurationData.stations)
  private val routeManager: RouteManager = mock[RouteManager]
  when(routeManager.routes).thenReturn(simpleConfigurationData.routes)
  private val trainManager: TrainManager = mock[TrainManager]
  when(trainManager.trains).thenReturn(simpleConfigurationData.trains.toList)
  private val timetableManager: TimetableManager = mock[TimetableManager]
  when(timetableManager.tables).thenReturn(simpleConfigurationData.timetables.values.flatten.toSeq)

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
    println(simulationData.simulationEnvironment.timetables)
    engine shouldBe Engine.emptyWithConfiguration(EngineConfiguration.defaultBatch())
    simulationData compareTo SimulationData.empty() ignoring SimulationEnvironment shouldBeBoolean true
    simulationData.simulationEnvironment shouldBe RailwayEnvironment.auto(simpleConfigurationData)

  "SimulationService" should:
    "init simulation" in:
      val initSimulationResult = simulationService.initSimulation()
      updateState()
      evaluateSettings.tupled(Await.result(initSimulationResult, Duration.Inf))
      eventQueue.events.clear()

    "setup engine" in:
      val ec                = EngineConfiguration(10, Some(10))
      val setupEngineResult = simulationService.setupEngine.tupled(ec.stepSize, ec.cyclesPerSecond)
      updateState()
      Await.result(setupEngineResult, Duration.Inf) shouldBe Some(Engine.emptyWithConfiguration(ec))
      eventQueue.events.clear()

    "fail to setup engine with negative step size" in:
      val ec                = (-1, Some(10))
      val setupEngineResult = simulationService.setupEngine.tupled(ec)
      updateState()
      Await.result(setupEngineResult, Duration.Inf) shouldBe None
      eventQueue.events.clear()

    "fail to setup engine with non-positive cycles per second" in:
      val ec                = (10, Some(0))
      val setupEngineResult = simulationService.setupEngine.tupled(ec)
      updateState()
      Await.result(setupEngineResult, Duration.Inf) shouldBe None
      eventQueue.events.clear()

    "maintain engine setup on init" in:
      val ec                   = EngineConfiguration(10, Some(10))
      val setupEngineResult    = simulationService.setupEngine(ec.stepSize, ec.cyclesPerSecond)
      val initSimulationResult = simulationService.initSimulation()
      updateState()
      Await.result(setupEngineResult, Duration.Inf) shouldBe Some(Engine.emptyWithConfiguration(ec))
      val (engine, _) = Await.result(initSimulationResult, Duration.Inf)
      engine shouldBe Engine.emptyWithConfiguration(ec)
      eventQueue.events.clear()

    "start simulation" in:
      val startSimulationResult = simulationService.start()
      updateState()
      Await.result(startSimulationResult, Duration.Inf).running shouldBe true
      eventQueue.events.clear()

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
      eventQueue.events.clear()

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
      eventQueue.events.clear()

    "reset simulation" in:
      simulationService.initSimulation()
      simulationService.start()
      val resetSimulationResult = simulationService.reset()
      updateState()
      evaluateSettings.tupled(Await.result(resetSimulationResult, Duration.Inf))
      eventQueue.events.clear()
