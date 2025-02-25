package ulisse.applications.useCases

import ulisse.applications.managers.SimulationManager
import ulisse.applications.ports.SimulationPorts
import ulisse.applications.AppState
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.simulation.Environments.RailwayEnvironment
import ulisse.entities.simulation.SimulationAgent
import ulisse.entities.simulation.Simulations.EngineState
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.infrastructures.commons.TimeProviders.*
import ulisse.utils.Times
import ulisse.utils.Times.Time

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

final case class SimulationService(
    private val eventQueue: LinkedBlockingQueue[AppState => AppState],
    private val notificationService: SimulationPorts.Output
) extends SimulationPorts.Input:

  override def initSimulation(): Future[EngineState] =
    val p = Promise[EngineState]()
    eventQueue.add((appState: AppState) => {
      val newSimulationManager = appState.simulationManager.setupEnvironment(RailwayEnvironment(
        appState.stationManager.stations.map(StationEnvironmentElement.apply),
        Seq[RouteEnvironmentElement](),
        Seq[SimulationAgent]()
      ))
      p.success(newSimulationManager.engineState)
      appState.copy(simulationManager = newSimulationManager)
    })
    p.future

  override def setupEngine(stepSize: Time, cyclesPerSecond: Option[Int]): Future[EngineState] = {
    val p = Promise[EngineState]()
    eventQueue.add((appState: AppState) => {
      val newSimulationManager = appState.simulationManager.setupEngine(stepSize, cyclesPerSecond)
      p.success(newSimulationManager.engineState)
      appState.copy(simulationManager = newSimulationManager)
    })
    p.future
  }

  def start(): Future[EngineState] =
    val p = Promise[EngineState]()
    eventQueue.offer((appState: AppState) => {
      val newSimulationManager = appState.simulationManager.start()
      p.success({ println("[SimulationService]: Simulation Started"); newSimulationManager.engineState })
      println("Start1")
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
      println("Start2")
      if appState.simulationManager.engineState.running then
        println("Start3")
        doStep()
        appState.copy(simulationManager = appState.simulationManager.doStep())
      else
        println("Start4")
        appState
    })
