package ulisse.applications.useCases

import ulisse.applications.EventQueue
import ulisse.applications.managers.SimulationManager
import ulisse.applications.ports.SimulationPorts
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.simulation.Environments.RailwayEnvironment
import ulisse.entities.simulation.SimulationAgent
import ulisse.entities.simulation.Simulations.{EngineState, SimulationData}
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.infrastructures.commons.TimeProviders.*

import scala.concurrent.{Future, Promise}

final case class SimulationService(
    private val eventQueue: EventQueue,
    private val notificationService: SimulationPorts.Output
) extends SimulationPorts.Input:

  override def initSimulation(): Future[(EngineState, SimulationData)] =
    val p = Promise[(EngineState, SimulationData)]()
    eventQueue.addUpdateSimulationEvent((simulationManager, stationManager) => {
      val newSimulationManager = simulationManager.setupEnvironment(RailwayEnvironment(
        stationManager.stations.map(StationEnvironmentElement.apply),
        Seq[RouteEnvironmentElement](),
        Seq[SimulationAgent]()
      ))
      p.success((newSimulationManager.engineState, newSimulationManager.simulationData))
      newSimulationManager
    })
    p.future

  override def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Future[Option[EngineState]] = {
    val p = Promise[Option[EngineState]]()
    eventQueue.addUpdateSimulationEvent((simulationManager, stationManager) => {
      simulationManager.setupEngine(stepSize, cyclesPerSecond) match
        case Some(newSimulationManager) =>
          p.success(Some(newSimulationManager.engineState))
          newSimulationManager
        case _ =>
          p.success(None)
          simulationManager
    })
    p.future
  }

  def start(): Future[EngineState] =
    val p = Promise[EngineState]()
    eventQueue.addUpdateSimulationEvent((simulationManager, _) => {
      val newSimulationManager = simulationManager.start()
      p.success({ println("[SimulationService]: Simulation Started"); newSimulationManager.engineState })
      println("Start1")
      doStep()
      newSimulationManager
    })
    p.future

  def stop(): Future[EngineState] =
    val p = Promise[EngineState]()
    eventQueue.addUpdateSimulationEvent((simulationManager, _) => {
      val newSimulationManager = simulationManager.stop()
      p.success({ println("[SimulationService]: Simulation Stopped"); newSimulationManager.engineState })
      newSimulationManager
    })
    p.future

  def reset(): Future[EngineState] =
    val p = Promise[EngineState]()
    eventQueue.addUpdateSimulationEvent((simulationManager, _) => {
      val newSimulationManager = simulationManager.reset()
      p.success({ println("[SimulationService]: Simulation Reset"); newSimulationManager.engineState })
      newSimulationManager
    })
    p.future

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def doStep(): Unit =
    eventQueue.addUpdateSimulationEvent((simulationManager, _) => {
      println("Start2")
      if simulationManager.engineState.running then
        println("Start3")
        val newSimulationManager = simulationManager.doStep()
        doStep()
        newSimulationManager
      else
        println("Start4")
        simulationManager
    })
