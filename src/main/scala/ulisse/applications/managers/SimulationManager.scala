package ulisse.applications.managers

import ulisse.applications.ports.{SimulationPorts, UtilityPorts}
import ulisse.entities.simulation.data.{EngineState, SimulationData}
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.utils.Times.{ClockTime, Time}

trait SimulationManager:
  def engineState: EngineState
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
      EngineState.empty().copy(cyclesPerSecond = cyclesPerSecond),
      SimulationData.empty(),
      notificationService,
      timeProvider
    )

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
      engineState: EngineState,
      simulationData: SimulationData,
      notificationService: Option[SimulationPorts.Output],
      timeProvider: UtilityPorts.Output.TimeProviderPort
  ) extends SimulationManager:
    override def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Option[SimulationManager] =
      Some(copy(engineState.copy(cyclesPerSecond = cyclesPerSecond, stepSize = stepSize)))
    override def setupEnvironment(environment: RailwayEnvironment): SimulationManager =
      copy(simulationData = SimulationData.withEnvironment(environment))
    override def start(): SimulationManager =
      copy(engineState.copy(true))
    override def stop(): SimulationManager  = copy(engineState.copy(false))
    override def reset(): SimulationManager = copy(engineState.reset(), simulationData.reset())
    override def doStep(): SimulationManager =
      def _updateSimulationData(engineData: EngineState, simulationData: SimulationData): SimulationData =
        val newSimulationData = simulationData.increaseStepByOne().increaseSecondElapsedBy(engineData.lastDelta)
        for ns <- notificationService do ns.stepNotification(newSimulationData)
        newSimulationData
      val updatedEngineData = engineState.update(timeProvider.currentTimeMillis().toDouble)
      updatedEngineData.cyclesPerSecond match
        case Some(cps) =>
          val cycleTimeStep = calculateCycleTimeStep(cps)
          println(
            s"Cycle Time Step: ${updatedEngineData}${updatedEngineData.elapsedCycleTime}, ${simulationData.secondElapsed}, ${updatedEngineData.lastDelta}"
          )
          if updatedEngineData.elapsedCycleTime >= cycleTimeStep then
            println(
              "decrease cycle timeStep"
            )
            copy(
              updatedEngineData.decreaseElapsedCycleTimeBy(cycleTimeStep),
              _updateSimulationData(updatedEngineData, simulationData)
            )
          else
            copy(updatedEngineData, simulationData.increaseSecondElapsedBy(updatedEngineData.lastDelta))
        case None =>
          println(
            s"newData $updatedEngineData"
          )
          val newSimData = _updateSimulationData(updatedEngineData, simulationData)
          copy(engineState = updatedEngineData, simulationData = newSimData)
