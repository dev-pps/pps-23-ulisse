package ulisse.applications.managers

import ulisse.applications.SimulationState
import ulisse.applications.ports.{SimulationPorts, UtilityPorts}
import ulisse.entities.simulation.Agents.SimulationAgent
import ulisse.entities.simulation.Environments.SimulationEnvironment
import ulisse.entities.simulation.Simulations.{EngineData, SimulationData}
import ulisse.entities.station.Station
import ulisse.infrastructures.commons.TimeProviders.TimeProvider

import java.util.concurrent.LinkedBlockingQueue

trait SimulationManager:
  def running: Boolean
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
      EngineData(false, None, None, 0, 0),
      SimulationData(0, 0, SimulationEnvironment.empty()),
      notificationService,
      timeProvider
    )
  private case class SimulationManagerImpl(
      engineData: EngineData,
      simulationData: SimulationData,
      notificationService: SimulationPorts.Output,
      timeProvider: UtilityPorts.Output.TimeProviderPort
  ) extends SimulationManager:
    override def start(environment: SimulationEnvironment): SimulationManager =
      copy(engineData.copy(true), simulationData.copy(simulationEnvironment = environment))
    override def stop(): SimulationManager  = copy(engineData.copy(false))
    override def reset(): SimulationManager = copy(engineData.copy(false), simulationData.clear())
    override def doStep(): SimulationManager =
      def _updateSimulationData(engineData: EngineData, simulationData: SimulationData): SimulationData =
        val newSimulationData = simulationData.copy(
          step = simulationData.step + 1,
          secondElapsed = simulationData.secondElapsed + engineData.lastDelta
        )
        notificationService.stepNotification(newSimulationData)
        newSimulationData
      val updatedEngineData = engineData.update(timeProvider.currentTimeMillis.toDouble)
      updatedEngineData.cyclesPerSecond match
        case Some(cps) =>
          val cycleTimeStep = 1.0 / cps
          println(
            s"Cycle Time Step: ${updatedEngineData.elapsedCycleTime}, ${simulationData.secondElapsed}, ${updatedEngineData.lastDelta}"
          )
          if updatedEngineData.elapsedCycleTime >= cycleTimeStep then
            val newSimData = _updateSimulationData(updatedEngineData, simulationData)
            copy(
              engineData =
                updatedEngineData.copy(elapsedCycleTime = updatedEngineData.elapsedCycleTime - cycleTimeStep),
              simulationData = newSimData
            )
          else
            copy(
              engineData = updatedEngineData,
              simulationData = simulationData.copy(secondElapsed = simulationData.secondElapsed + engineData.lastDelta)
            )
        case None =>
          val newSimData = _updateSimulationData(updatedEngineData, simulationData)
          copy(engineData = updatedEngineData, simulationData = newSimData)

    export engineData.running
