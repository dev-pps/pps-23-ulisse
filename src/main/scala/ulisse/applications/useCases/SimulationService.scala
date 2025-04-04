package ulisse.applications.useCases

import ulisse.applications.configs.RailwayConfig
import ulisse.applications.events.SimulationEventQueue
import ulisse.applications.managers.SimulationManager
import ulisse.applications.ports.SimulationPorts
import ulisse.applications.ports.SimulationPorts.Input.Preset
import ulisse.applications.ports.SimulationPorts.Input.Preset.*
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.simulation.data.{Engine, SimulationData}
import ulisse.entities.simulation.environments.railwayEnvironment.{ConfigurationData, RailwayEnvironment}
import ulisse.entities.station.StationEnvironmentElement
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.infrastructures.utils.TimeProviders.*

import scala.concurrent.{Future, Promise}
import scala.util.chaining.scalaUtilChainingOps

/** Service for simulation. */
final case class SimulationService(
    private val eventQueue: SimulationEventQueue,
    private val notificationService: SimulationPorts.Output
) extends SimulationPorts.Input:
  val minPermittedDistanceBetweenTrains: Double = 100.0
  eventQueue.addUpdateSimulationManagerEvent(_.withNotificationService(Some(notificationService)))

  override def initSimulation(): Future[(Engine, SimulationData)] =
    val p = Promise[(Engine, SimulationData)]()
    eventQueue.addSetupSimulationManagerEvent(simulationManagers =>
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

  override def initSimulationUsing(preset: Preset): Future[(Engine, SimulationData)] =
    eventQueue.addSetupAppStateEvent(_ =>
      preset match
        case Simple   => RailwayConfig.simpleRailwayConfig
        case Complex  => RailwayConfig.complexRailwayConfig
        case Examples => RailwayConfig.exampleRailwayConfig
    )
    eventQueue.addUpdateSimulationManagerEvent(_.withNotificationService(Some(notificationService)))
    initSimulation()

  override def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Future[Option[Engine]] =
    val p = Promise[Option[Engine]]()
    eventQueue.addUpdateSimulationManagerEvent(simulationManager =>
      val newSimulationManager = simulationManager.setupEngine(stepSize, cyclesPerSecond)
      p.success(newSimulationManager.map(_.engine))
      newSimulationManager.getOrElse(simulationManager)
    )
    p.future

  override def start(): Future[Engine] =
    val p = Promise[Engine]()
    eventQueue.addUpdateSimulationManagerEvent(_.start().tap(sm => { p.success(sm.engine); doStep() }))
    p.future

  override def stop(): Future[Engine] =
    val p = Promise[Engine]()
    eventQueue.addUpdateSimulationManagerEvent(_.stop().tap(sm => p.success(sm.engine)))
    p.future

  override def reset(): Future[(Engine, SimulationData)] =
    val p = Promise[(Engine, SimulationData)]()
    eventQueue.addUpdateSimulationManagerEvent(_.reset().tap(sm => p.success(sm.engine, sm.simulationData)))
    p.future

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def doStep(): Unit =
    eventQueue.addUpdateSimulationManagerEvent:
      case sm: SimulationManager if sm.engine.running => doStep(); sm.doStep()
      case sm                                         => sm
