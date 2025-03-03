package ulisse.applications.useCases

import ulisse.applications.managers.SimulationManager
import ulisse.applications.ports.SimulationPorts
import ulisse.applications.AppState
import ulisse.applications.event.SimulationEventQueue
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.simulation.data.{EngineState, SimulationData}
import ulisse.entities.simulation.agents.SimulationAgent
import ulisse.entities.simulation.environments.railwayEnvironment.{ConfigurationData, RailwayEnvironment}
import ulisse.entities.station.Station
import ulisse.entities.station.StationEnvironments.StationEnvironmentElement
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.infrastructures.commons.TimeProviders.*
import ulisse.utils.Times
import ulisse.utils.Times.Time

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

final case class SimulationService(
    private val eventQueue: SimulationEventQueue,
    private val notificationService: SimulationPorts.Output
) extends SimulationPorts.Input:
  private val minPermittedDistanceBetweenTrains: Double = 100.0
  override def initSimulation(): Future[(EngineState, SimulationData)] =
    val p = Promise[(EngineState, SimulationData)]()
    eventQueue.addUpdateSimulationEvent(
      (simulationManager, stationManager, routeManager, trainManager, timetableManager) => {
        val newSimulationManager = simulationManager.setupEnvironment(RailwayEnvironment(
          Time(0, 0, 0),
          ConfigurationData(
            stationManager.stations.map(StationEnvironmentElement(_)),
            routeManager.routes.map(RouteEnvironmentElement(_, minPermittedDistanceBetweenTrains)),
            trainManager.trains.map(TrainAgent(_)),
            timetableManager.tables.map(DynamicTimetable(_))
          )
        ))
        p.success((newSimulationManager.engineState, newSimulationManager.simulationData))
        newSimulationManager
      }
    )
    p.future

  override def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Future[Option[EngineState]] = {
    val p = Promise[Option[EngineState]]()
    eventQueue.addUpdateSimulationEvent(
      (simulationManager, stationManager, routeManager, trainManager, timetableManager) => {
        simulationManager.setupEngine(stepSize, cyclesPerSecond) match
          case Some(newSimulationManager) =>
            p.success(Some(newSimulationManager.engineState))
            newSimulationManager
          case _ =>
            p.success(None)
            simulationManager
      }
    )
    p.future
  }

  def start(): Future[EngineState] =
    val p = Promise[EngineState]()
    eventQueue.addUpdateSimulationEvent(
      (simulationManager, stationManager, routeManager, trainManager, timetableManager) => {
        val newSimulationManager = simulationManager.start()
        p.success({ println("[SimulationService]: Simulation Started"); newSimulationManager.engineState })
        println("Start1")
        doStep()
        newSimulationManager
      }
    )
    p.future

  def stop(): Future[EngineState] =
    val p = Promise[EngineState]()
    eventQueue.addUpdateSimulationEvent(
      (simulationManager, stationManager, routeManager, trainManager, timetableManager) => {
        val newSimulationManager = simulationManager.stop()
        p.success({ println("[SimulationService]: Simulation Stopped"); newSimulationManager.engineState })
        newSimulationManager
      }
    )
    p.future

  def reset(): Future[EngineState] =
    val p = Promise[EngineState]()
    eventQueue.addUpdateSimulationEvent(
      (simulationManager, stationManager, routeManager, trainManager, timetableManager) => {
        val newSimulationManager = simulationManager.reset()
        p.success({ println("[SimulationService]: Simulation Reset"); newSimulationManager.engineState })
        newSimulationManager
      }
    )
    p.future

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def doStep(): Unit =
    eventQueue.addUpdateSimulationEvent(
      (simulationManager, stationManager, routeManager, trainManager, timetableManager) => {
        println("Start2")
        if simulationManager.engineState.running then
          println("Start3")
          doStep()
          simulationManager.doStep()
        else
          println("Start4")
          simulationManager
      }
    )
