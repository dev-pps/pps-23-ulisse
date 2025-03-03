package ulisse.applications.managers

import ulisse.applications.ports.{SimulationPorts, UtilityPorts}
import ulisse.entities.simulation.data.{Engine, EngineConfiguration, EngineState, SimulationData}
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.utils.Times.{ClockTime, Time}
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

object SimulationManager:
  def apply(
      notificationService: Option[SimulationPorts.Output],
      timeProvider: UtilityPorts.Output.TimeProviderPort,
      cyclesPerSecond: Option[Int]
  ): SimulationManager =
    SimulationManagerImpl(
      Engine.emptyWithConfiguration(EngineConfiguration.withCyclesPerSecond(cyclesPerSecond)),
      SimulationData.empty(),
      notificationService,
      timeProvider
    )

  /** Calculate how often a cycle should occur in milliseconds. */
  def calculateCycleTimeStep(cps: Int): Double =
    1.0 / cps * 1000

  def timedManager(
      notificationService: SimulationPorts.Output,
      timeProvider: UtilityPorts.Output.TimeProviderPort,
      cyclesPerSecond: Int
  ): SimulationManager = apply(Some(notificationService), timeProvider, Some(cyclesPerSecond))

  def emptyTimedManager(timeProvider: UtilityPorts.Output.TimeProviderPort, cyclesPerSecond: Int): SimulationManager =
    apply(None, timeProvider, Some(cyclesPerSecond))

  def batchManager(
      notificationService: SimulationPorts.Output,
      timeProvider: UtilityPorts.Output.TimeProviderPort
  ): SimulationManager = apply(Some(notificationService), timeProvider, None)

  def emptyBatchManager(timeProvider: UtilityPorts.Output.TimeProviderPort): SimulationManager =
    apply(None, timeProvider, None)

  extension (simulationManager: SimulationManager)
    def withNotificationService(notificationService: SimulationPorts.Output): SimulationManager =
      simulationManager match
        case SimulationManagerImpl(engineState, simulationData, _, timeProvider) =>
          SimulationManagerImpl(engineState, simulationData, Some(notificationService), timeProvider)

  private case class SimulationManagerImpl(
      engine: Engine,
      simulationData: SimulationData,
      notificationService: Option[SimulationPorts.Output],
      timeProvider: UtilityPorts.Output.TimeProviderPort
  ) extends SimulationManager:
    override def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Option[SimulationManager] =
      Some(copy(engine.configuration = EngineConfiguration(stepSize, cyclesPerSecond)))
    override def setupEnvironment(environment: RailwayEnvironment): SimulationManager =
      copy(simulationData = SimulationData.withEnvironment(environment))
    override def start(): SimulationManager =
      copy(engine.running = true)
    override def stop(): SimulationManager  = copy(engine.running = false)
    override def reset(): SimulationManager = copy(engine.reset(), simulationData.reset())

    private def updateSimulationData(engineData: EngineState): SimulationData =
      simulationData
        .increaseStepByOne()
        .increaseSecondElapsedBy(engineData.lastDelta)
        .tap(nsd => notificationService.foreach(_.stepNotification(nsd)))

    private def updateManager(es: EngineState => EngineState, sd: EngineState => SimulationData)(using
        engineState: EngineState
    ): SimulationManager =
      copy(engine.state = es(engineState), sd(engineState))

    private def increaseSecondElapsedData(engineState: EngineState): SimulationData =
      simulationData.increaseSecondElapsedBy(engineState.lastDelta)

    override def doStep(): SimulationManager =
      given updatedEngineState: EngineState = engine.state.update(timeProvider.currentTimeMillis().toDouble)
      engine.configuration.cyclesPerSecond.map(calculateCycleTimeStep) match
        case Some(cycleTimeStep) if updatedEngineState.elapsedCycleTime >= cycleTimeStep =>
          updateManager(_.updateElapsedCycleTime(-cycleTimeStep), updateSimulationData)
        case Some(_) => updateManager(identity, increaseSecondElapsedData)
        case _       => updateManager(identity, updateSimulationData)
