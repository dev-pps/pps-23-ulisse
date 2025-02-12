package ulisse.applications.useCases

import ulisse.adapters.output.UtilityAdapters.TimeProviderAdapter
import ulisse.applications.managers.SimulationManager
import ulisse.applications.{AppState, SimulationState}
import ulisse.applications.ports.{SimulationPorts, UtilityPorts}
import ulisse.entities.Coordinate
import ulisse.entities.simulation.Agents.SimulationAgent
import ulisse.entities.simulation.Environments.SimulationEnvironment
import ulisse.entities.simulation.Simulations.EngineState
import ulisse.entities.station.Station
import ulisse.infrastructures.commons.TimeProviders.*

import java.util.concurrent.{Executors, LinkedBlockingQueue}
import scala.concurrent.{ExecutionContext, Future, Promise}

final case class SimulationService(
    eventQueue: LinkedBlockingQueue[AppState => AppState],
    notificationService: SimulationPorts.Output,
    timeProviderService: UtilityPorts.Output.TimeProviderPort
) extends SimulationPorts.Input:
  def start(): Future[EngineState] =
    val p = Promise[EngineState]()
    eventQueue.add((appState: AppState) => {
      simulationEvents.add((state: SimulationState) => {
        val newSimulationManager =
          state.simulationManager.withNotificationService(notificationService).start(SimulationEnvironment(
            appState.stationManager.stations,
            Seq[SimulationAgent]()
          ))
        p.success({ println("[SimulationService]: Simulation Started"); newSimulationManager.engineState })
        doStep()
        state.copy(simulationManager = newSimulationManager)
      })
      appState
    })
    p.future

  def stop(): Future[EngineState] =
    val p = Promise[EngineState]()
    simulationEvents.add((state: SimulationState) => {
      val newSimulationManager = state.simulationManager.stop()
      p.success({ println("[SimulationService]: Simulation Stopped"); newSimulationManager.engineState })
      state.copy(simulationManager = newSimulationManager)
    })
    p.future

  def reset(): Future[EngineState] =
    val p = Promise[EngineState]()
    simulationEvents.add((state: SimulationState) => {
      val newSimulationManager = state.simulationManager.reset()
      p.success({ println("[SimulationService]: Simulation Reset"); newSimulationManager.engineState })
      state.copy(simulationManager = newSimulationManager)
    })
    p.future

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def doStep(): Unit =
    simulationEvents.offer((state: SimulationState) => {
      if state.simulationManager.engineState.running then
        doStep()
        state.copy(simulationManager = state.simulationManager.doStep())
      else
        state
    })
