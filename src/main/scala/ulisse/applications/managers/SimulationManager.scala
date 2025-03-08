package ulisse.applications.managers

import ulisse.applications.ports.{SimulationPorts, UtilityPorts}
import ulisse.entities.simulation.data.{Engine, EngineConfiguration, EngineState, SimulationData}
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment

import scala.util.chaining.scalaUtilChainingOps

/** Manager for the simulation. */
trait SimulationManager:
  /** Engine of the simulation. */
  def engine: Engine

  /** Data of the simulation. */
  def simulationData: SimulationData

  /** Set up the engine of the simulation. */
  def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Option[SimulationManager]

  /** Set up the environment of the simulation. */
  def setupEnvironment(environment: RailwayEnvironment): SimulationManager

  /** Start the simulation. */
  def start(): SimulationManager

  /** Stop the simulation. */
  def stop(): SimulationManager

  /** Reset the simulation. */
  def reset(): SimulationManager

  /** Perform a step of the simulation. */
  def doStep(): SimulationManager

  /** Set the notification service. */
  def withNotificationService(notificationService: Option[SimulationPorts.Output]): SimulationManager

/** Factory for [[SimulationManager]] instances. */
object SimulationManager:
  /** Create a new simulation manager. */
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

  /** Create a new simulation manager with the given configuration. */
  def configuredManager(
      timeProvider: UtilityPorts.Output.TimeProviderPort,
      engineConfiguration: EngineConfiguration
  ): SimulationManager =
    SimulationManager(None, timeProvider, engineConfiguration)

  /** Create a new simulation manager with the default timed configuration. */
  def defaultTimedManager(
      timeProvider: UtilityPorts.Output.TimeProviderPort
  ): SimulationManager = SimulationManager(None, timeProvider, EngineConfiguration.defaultTimed())

  /** Create a new simulation manager with the default batch configuration. */
  def defaultBatchManager(
      timeProvider: UtilityPorts.Output.TimeProviderPort
  ): SimulationManager = SimulationManager(None, timeProvider, EngineConfiguration.defaultBatch())

  /** Calculate how often a cycle should occur in milliseconds. */
  def calculateCycleTimeStep(cps: Int): Long =
    (1.0 / cps * 1000).toLong

  private case class SimulationManagerImpl(
      engine: Engine,
      simulationData: SimulationData,
      notificationService: Option[SimulationPorts.Output],
      timeProvider: UtilityPorts.Output.TimeProviderPort
  ) extends SimulationManager:
    override def start(): SimulationManager = copy(engine.running = true)
    override def stop(): SimulationManager  = copy(engine.running = false)
    override def reset(): SimulationManager = copy(engine.reset(), simulationData.reset())

    override def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Option[SimulationManager] =
      EngineConfiguration.createCheckedConfiguration(stepSize, cyclesPerSecond).map: ec =>
        copy(engine.configuration = ec)
    override def setupEnvironment(environment: RailwayEnvironment): SimulationManager =
      copy(simulationData = SimulationData.withEnvironment(environment))
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
      val timetables = simulationManager.simulationData.simulationEnvironment.timetables

      if timetables.nonEmpty && timetables.forall(_.completed) then
        notificationService.foreach(_.simulationEnded(simulationManager.simulationData))
        simulationManager.stop()
      else simulationManager

    override def doStep(): SimulationManager =
      given updatedEngineState: EngineState = engine.state.update(timeProvider.currentTimeMillis())
      if engine.running then evaluateTermination(updateSimulation) else this
