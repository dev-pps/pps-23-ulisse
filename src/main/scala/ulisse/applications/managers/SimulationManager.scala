package ulisse.applications.managers

import ulisse.applications.ports.{SimulationPorts, UtilityPorts}
import ulisse.entities.simulation.data.{Engine, EngineConfiguration, EngineState, SimulationData}
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.utils.Times.{ClockTime, Time}

import scala.collection.immutable.{AbstractSeq, LinearSeq}
import scala.util.chaining.scalaUtilChainingOps

trait SimulationManager:
  def engine: Engine
  def simulationData: SimulationData
  def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Option[SimulationManager]
  def setupEnvironment(environment: RailwayEnvironment): SimulationManager
  def start(): SimulationManager
  def stop(): SimulationManager
  def reset(): SimulationManager
  def doStep(): SimulationManager
  def withNotificationService(notificationService: Option[SimulationPorts.Output]): SimulationManager

object SimulationManager:
  def apply(
      notificationService: Option[SimulationPorts.Output],
      timeProvider: UtilityPorts.Output.TimeProviderPort,
      engineConfiguration: EngineConfiguration
  ): SimulationManager =
    SimulationManagerImpl(
      Engine.emptyWithConfiguration(engineConfiguration),
      SimulationData.empty(),
      notificationService,
      timeProvider
    )

  def configuredManager(
      timeProvider: UtilityPorts.Output.TimeProviderPort,
      engineConfiguration: EngineConfiguration
  ): SimulationManager =
    SimulationManager(None, timeProvider, engineConfiguration)

  def defaultTimedManager(
      timeProvider: UtilityPorts.Output.TimeProviderPort
  ): SimulationManager = SimulationManager(None, timeProvider, EngineConfiguration.defaultTimed())

  def defaultBatchManager(
      timeProvider: UtilityPorts.Output.TimeProviderPort
  ): SimulationManager = SimulationManager(None, timeProvider, EngineConfiguration.defaultBatch())

  /** Calculate how often a cycle should occur in milliseconds. */
  def calculateCycleTimeStep(cps: Int): Double =
    1.0 / cps * 1000

  private case class SimulationManagerImpl(
      engine: Engine,
      simulationData: SimulationData,
      notificationService: Option[SimulationPorts.Output],
      timeProvider: UtilityPorts.Output.TimeProviderPort
  ) extends SimulationManager:
    override def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Option[SimulationManager] =
      EngineConfiguration.createCheckedConfiguration(stepSize, cyclesPerSecond).map(ec =>
        copy(engine.configuration = ec)
      )
    override def setupEnvironment(environment: RailwayEnvironment): SimulationManager =
      copy(simulationData = SimulationData.withEnvironment(environment))
    override def start(): SimulationManager =
      copy(engine.running = true)
    override def stop(): SimulationManager  = copy(engine.running = false)
    override def reset(): SimulationManager = copy(engine.reset(), simulationData.reset())
    override def withNotificationService(notificationService: Option[SimulationPorts.Output]): SimulationManager =
      copy(notificationService = notificationService)
    private def updateSimulationData(engineData: EngineState): SimulationData =
      simulationData
        .increaseStepByOne()
        .increaseMillisecondsElapsedBy(engineData.lastDelta)
        .simulationEnvironment_=(simulationData.simulationEnvironment.doStep(engine.configuration.stepSize))
        .tap(nsd => notificationService.foreach(_.stepNotification(nsd)))

    private def updateManager(es: EngineState => EngineState, sd: EngineState => SimulationData)(using
        engineState: EngineState
    ): SimulationManager =
      copy(engine.state = es(engineState), sd(engineState))

    private def increaseSecondElapsedData(engineState: EngineState): SimulationData =
      simulationData.increaseMillisecondsElapsedBy(engineState.lastDelta)

    private def updateSimulation(using engineState: EngineState): SimulationManager =
      engine.configuration.cyclesPerSecond.map(calculateCycleTimeStep) match
        case Some(cycleTimeStep) if engineState.elapsedCycleTime >= cycleTimeStep =>
          updateManager(_.updateElapsedCycleTime(-cycleTimeStep), updateSimulationData)
        case Some(_) => updateManager(identity, increaseSecondElapsedData)
        case _       => updateManager(identity, updateSimulationData)

    private def evaluateTermination(simulationManager: SimulationManager): SimulationManager =
      if simulationData.simulationEnvironment.timetables.forall(_.completed) then
        notificationService.foreach(_.simulationEnded(simulationData))
        simulationManager.stop()
      else simulationManager

    override def doStep(): SimulationManager =
      given updatedEngineState: EngineState = engine.state.update(timeProvider.currentTimeMillis().toDouble)
      evaluateTermination(updateSimulation)
