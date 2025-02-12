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
  def start(environment: SimulationEnvironment): SimulationManager
  def stop(): SimulationManager
  def reset(): SimulationManager
  def doStep(): SimulationManager

object SimulationManager:
  def apply(
      notificationService: SimulationPorts.Output,
      timeProvider: UtilityPorts.Output.TimeProviderPort
  ): SimulationManager =
    SimulationManagerImpl(
      EngineState(false, None, None, 0, 0),
      SimulationData(0, 0, SimulationEnvironment.empty()),
      notificationService,
      timeProvider
    )
  private case class SimulationManagerImpl(
      engineState: EngineState,
      simulationData: SimulationData,
      notificationService: SimulationPorts.Output,
      timeProvider: UtilityPorts.Output.TimeProviderPort
  ) extends SimulationManager:
    override def start(environment: SimulationEnvironment): SimulationManager =
      copy(engineState.copy(true), simulationData.copy(simulationEnvironment = environment))
    override def stop(): SimulationManager  = copy(engineState.copy(false))
    override def reset(): SimulationManager = copy(engineState.copy(false), simulationData.clear())
    override def doStep(): SimulationManager =
      def _updateSimulationData(engineData: EngineState, simulationData: SimulationData): SimulationData =
        val newSimulationData = simulationData.copy(
          step = simulationData.step + 1,
          secondElapsed = simulationData.secondElapsed + engineData.lastDelta
        )
        notificationService.stepNotification(newSimulationData)
        newSimulationData
      val updatedEngineData = engineState.update(timeProvider.currentTimeMillis.toDouble)
      updatedEngineData.cyclesPerSecond match
        case Some(cps) =>
          val cycleTimeStep = 1.0 / cps
          println(
            s"Cycle Time Step: ${updatedEngineData.elapsedCycleTime}, ${simulationData.secondElapsed}, ${updatedEngineData.lastDelta}"
          )
          if updatedEngineData.elapsedCycleTime >= cycleTimeStep then
            val newSimData = _updateSimulationData(updatedEngineData, simulationData)
            copy(
              engineState =
                updatedEngineData.copy(elapsedCycleTime = updatedEngineData.elapsedCycleTime - cycleTimeStep),
              simulationData = newSimData
            )
          else
            copy(
              engineState = updatedEngineData,
              simulationData = simulationData.copy(secondElapsed = simulationData.secondElapsed + engineState.lastDelta)
            )
        case None =>
          val newSimData = _updateSimulationData(updatedEngineData, simulationData)
          copy(engineState = updatedEngineData, simulationData = newSimData)
