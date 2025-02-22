package ulisse.applications.useCases

import ulisse.adapters.output.UtilityAdapters.TimeProviderAdapter
import ulisse.applications.managers.SimulationManager
import ulisse.applications.{AppState, SimulationState}
import ulisse.applications.ports.{SimulationPorts, UtilityPorts}
import ulisse.entities.Coordinate
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.simulation.SimulationAgent
import ulisse.entities.simulation.Environments.RailwayEnvironment
import ulisse.entities.simulation.Simulations.EngineState
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.infrastructures.commons.TimeProviders.*

import java.util.concurrent.{Executors, LinkedBlockingQueue}
import scala.concurrent.{ExecutionContext, Future, Promise}

final case class SimulationService(
    eventQueue: LinkedBlockingQueue[AppState => AppState],
    simulationEvents: LinkedBlockingQueue[SimulationState => SimulationState],
    notificationService: SimulationPorts.Output
) extends SimulationPorts.Input:
  eventQueue.add((appState: AppState) => {
    simulationEvents.add((state: SimulationState) => {
      state.copy(simulationManager =
        state.simulationManager.withNotificationService(notificationService).setup(RailwayEnvironment(
          appState.stationManager.stations.map(StationEnvironmentElement.apply),
          Seq[RouteEnvironmentElement](),
          Seq[SimulationAgent]()
        ))
      )
    })
    appState
  })

  def start(): Future[EngineState] =
    val p = Promise[EngineState]()
    eventQueue.add((appState: AppState) => {
      simulationEvents.add((state: SimulationState) => {
        val newSimulationManager = state.simulationManager.start()
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
