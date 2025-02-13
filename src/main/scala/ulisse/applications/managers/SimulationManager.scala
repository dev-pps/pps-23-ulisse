package ulisse.applications.managers

import ulisse.applications.SimulationState
import ulisse.applications.ports.{SimulationPorts, UtilityPorts}
import ulisse.entities.simulation.Agents.SimulationAgent
import ulisse.entities.simulation.Environments.SimulationEnvironment
import ulisse.entities.simulation.Simulations.{EngineState, SimulationData}
import ulisse.entities.station.Station
import ulisse.infrastructures.commons.TimeProviders.TimeProvider

import java.util.concurrent.LinkedBlockingQueue

trait SimulationManager:
  def engineState: EngineState
  def simulationData: SimulationData
  def setup(environment: SimulationEnvironment): SimulationManager
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
      EngineState(false, cyclesPerSecond, None, 0, 0),
      SimulationData(0, 0, SimulationEnvironment.empty()),
      notificationService,
      timeProvider
    )

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
    override def setup(environment: SimulationEnvironment): SimulationManager =
      copy(simulationData = simulationData.copy(simulationEnvironment = environment))
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
          val cycleTimeStep = 1.0 / cps
          println(
            s"Cycle Time Step: ${updatedEngineData}${updatedEngineData.elapsedCycleTime}, ${simulationData.secondElapsed}, ${updatedEngineData.lastDelta}"
          )
          if updatedEngineData.elapsedCycleTime / 1000.0 >= cycleTimeStep then
            println(
              "decrease cycle timeStep"
            )
            copy(
              updatedEngineData.decreaseElapsedCycleTimeBy(cycleTimeStep),
              _updateSimulationData(updatedEngineData, simulationData)
            )
          else
            copy(updatedEngineData, simulationData.increaseSecondElapsedBy(engineState.lastDelta))
        case None =>
          println(
            s"newData $updatedEngineData"
          )
          val newSimData = _updateSimulationData(updatedEngineData, simulationData)
          copy(engineState = updatedEngineData, simulationData = newSimData)
