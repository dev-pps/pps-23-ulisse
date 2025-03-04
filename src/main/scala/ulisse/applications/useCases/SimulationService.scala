package ulisse.applications.useCases

import ulisse.applications.managers.SimulationManager
import ulisse.applications.ports.SimulationPorts
import ulisse.applications.AppState
import ulisse.applications.event.SimulationEventQueue
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.simulation.data.{Engine, SimulationData}
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
import scala.util.chaining.scalaUtilChainingOps

final case class SimulationService(
    private val eventQueue: SimulationEventQueue,
    private val notificationService: SimulationPorts.Output
) extends SimulationPorts.Input:
  private val minPermittedDistanceBetweenTrains: Double = 100.0
  override def initSimulation(): Future[(Engine, SimulationData)] =
    val p = Promise[(Engine, SimulationData)]()
    eventQueue.setupSimulationManager(simulationManagers =>
      simulationManagers.simulationManager.reset().setupEnvironment(RailwayEnvironment.auto(
        ConfigurationData(
          simulationManagers.stationManager.stations.map(StationEnvironmentElement(_)),
          simulationManagers.routeManager.routes.map(RouteEnvironmentElement(_, minPermittedDistanceBetweenTrains)),
          simulationManagers.trainManager.trains.map(TrainAgent(_)),
          simulationManagers.timetableManager.tables.map(DynamicTimetable(_))
        )
      )).tap(nsm => p.success((nsm.engine, nsm.simulationData)))
    )
    p.future

  override def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Future[Option[Engine]] = {
    val p = Promise[Option[Engine]]()
    eventQueue.updateSimulationManager(simulationManager =>
      val newSimulationManager = simulationManager.setupEngine(stepSize, cyclesPerSecond)
      p.success(newSimulationManager.map(_.engine))
      newSimulationManager.getOrElse(simulationManager)
    )
    p.future
  }

  def start(): Future[Engine] =
    val p = Promise[Engine]()
    eventQueue.updateSimulationManager(_.start().tap(sm => { p.success(sm.engine); doStep() }))
    p.future

  def stop(): Future[Engine] =
    val p = Promise[Engine]()
    eventQueue.updateSimulationManager(_.stop().tap(sm => p.success(sm.engine)))
    p.future

  def reset(): Future[Engine] =
    val p = Promise[Engine]()
    eventQueue.updateSimulationManager(_.reset().tap(sm => p.success(sm.engine)))
    p.future

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def doStep(): Unit =
    eventQueue.updateSimulationManager {
        case sm: SimulationManager if sm.engine.running => doStep(); sm.doStep()
        case sm => sm
    }
