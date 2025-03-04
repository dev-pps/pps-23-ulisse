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
    eventQueue.addUpdateSimulationEvent(simulationEventData =>
      simulationEventData.simulationManager.reset().setupEnvironment(RailwayEnvironment.auto(
        ConfigurationData(
          simulationEventData.stationManager.stations.map(StationEnvironmentElement(_)),
          simulationEventData.routeManager.routes.map(RouteEnvironmentElement(_, minPermittedDistanceBetweenTrains)),
          simulationEventData.trainManager.trains.map(TrainAgent(_)),
          simulationEventData.timetableManager.tables.map(DynamicTimetable(_))
        )
      )).tap(nsm => p.success((nsm.engine, nsm.simulationData)))
    )
    p.future

  override def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Future[Option[Engine]] = {
    val p = Promise[Option[Engine]]()
    eventQueue.addUpdateSimulationEvent(simulationEventData =>
      val newSimulationManager = simulationEventData.simulationManager.setupEngine(stepSize, cyclesPerSecond)
      p.success(newSimulationManager.map(_.engine))
      newSimulationManager.getOrElse(simulationEventData.simulationManager)
    )
    p.future
  }

  def start(): Future[Engine] =
    val p = Promise[Engine]()
    eventQueue.addUpdateSimulationEvent(_.simulationManager.start().tap(sm => { p.success(sm.engine); doStep() }))
    p.future

  def stop(): Future[Engine] =
    val p = Promise[Engine]()
    eventQueue.addUpdateSimulationEvent(_.simulationManager.stop().tap(sm => p.success(sm.engine)))
    p.future

  def reset(): Future[Engine] =
    val p = Promise[Engine]()
    eventQueue.addUpdateSimulationEvent(_.simulationManager.reset().tap(sm => p.success(sm.engine)))
    p.future

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def doStep(): Unit =
    eventQueue.addUpdateSimulationEvent(simulationEventData =>
      simulationEventData.simulationManager match
        case sm: SimulationManager if sm.engine.running => doStep(); sm.doStep()
        case other                                      => other
    )
