package ulisse.applications.useCases

import ulisse.applications.managers.SimulationManager
import ulisse.applications.ports.SimulationPorts
import ulisse.applications.AppState
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.simulation.Environments.RailwayEnvironment
import ulisse.entities.simulation.SimulationAgent
import ulisse.entities.simulation.Simulations.{EngineState, SimulationData}
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.timetable.DynamicTimetable
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.infrastructures.commons.TimeProviders.*
import ulisse.utils.Times
import ulisse.utils.Times.Time

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

final case class SimulationService(
    private val eventQueue: LinkedBlockingQueue[AppState => AppState],
    private val notificationService: SimulationPorts.Output
) extends SimulationPorts.Input:
  private val minPermittedDistanceBetweenTrains: Double = 100.0
  override def initSimulation(): Future[(EngineState, SimulationData)] =
    val p = Promise[(EngineState, SimulationData)]()
    eventQueue.add((appState: AppState) => {
      val newSimulationManager = appState.simulationManager.setupEnvironment(RailwayEnvironment(
        appState.stationManager.stations.map(StationEnvironmentElement(_)),
        appState.routeManager.routes.map(RouteEnvironmentElement(_, minPermittedDistanceBetweenTrains)),
        appState.trainManager.trains.map(TrainAgent(_)),
        appState.timetableManager.tables.map(DynamicTimetable(_))
      ))
      p.success((newSimulationManager.engineState, newSimulationManager.simulationData))
      appState.copy(simulationManager = newSimulationManager)
    })
    p.future

  override def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Future[Option[EngineState]] = {
    val p = Promise[Option[EngineState]]()
    eventQueue.add((appState: AppState) => {
      appState.simulationManager.setupEngine(stepSize, cyclesPerSecond) match
        case Some(newSimulationManager) =>
          p.success(Some(newSimulationManager.engineState))
          appState.copy(simulationManager = newSimulationManager)
        case _ =>
          p.success(None)
          appState
    })
    p.future
  }

  def start(): Future[EngineState] =
    val p = Promise[EngineState]()
    eventQueue.offer((appState: AppState) => {
      val newSimulationManager = appState.simulationManager.start()
      p.success({ println("[SimulationService]: Simulation Started"); newSimulationManager.engineState })
      doStep()
      appState.copy(simulationManager = newSimulationManager)
    })
    p.future

  def stop(): Future[EngineState] =
    val p = Promise[EngineState]()
    eventQueue.add((appState: AppState) => {
      val newSimulationManager = appState.simulationManager.stop()
      p.success({ println("[SimulationService]: Simulation Stopped"); newSimulationManager.engineState })
      appState.copy(simulationManager = newSimulationManager)
    })
    p.future

  def reset(): Future[EngineState] =
    val p = Promise[EngineState]()
    eventQueue.add((appState: AppState) => {
      val newSimulationManager = appState.simulationManager.reset()
      p.success({ println("[SimulationService]: Simulation Reset"); newSimulationManager.engineState })
      appState.copy(simulationManager = newSimulationManager)
    })
    p.future

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def doStep(): Unit =
    eventQueue.offer((appState: AppState) => {
      if appState.simulationManager.engineState.running then
        doStep()
        appState.copy(simulationManager = appState.simulationManager.doStep())
      else
        appState
    })
